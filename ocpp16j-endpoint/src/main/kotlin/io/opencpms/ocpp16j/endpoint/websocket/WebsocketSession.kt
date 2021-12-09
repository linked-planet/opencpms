/**
 * OpenCPMS
 * Copyright (C) 2021 linked-planet GmbH (info@linked-planet.com).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.application
import io.opencpms.ocpp16.protocol.Ocpp16Error
import io.opencpms.ocpp16.protocol.Ocpp16IncomingResponse
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingRequest
import io.opencpms.ocpp16.service.receiver.Ocpp16MessageReceiver
import io.opencpms.ocpp16.service.session.Ocpp16Session
import io.opencpms.ocpp16.service.session.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.json.WebsocketMessageDeserializer
import io.opencpms.ocpp16j.endpoint.json.serialize
import io.opencpms.ocpp16j.endpoint.protocol.CallError
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.IncomingCall
import io.opencpms.ocpp16j.endpoint.protocol.IncomingCallResult
import io.opencpms.ocpp16j.endpoint.protocol.OutgoingCall
import io.opencpms.ocpp16j.endpoint.protocol.OutgoingCallResult
import io.opencpms.ocpp16j.endpoint.protocol.toCallError
import io.opencpms.ocpp16j.endpoint.protocol.toOcpp16Error
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(DefaultWebSocketServerSession::class.java)

fun DefaultWebSocketServerSession.ocpp16Session(
    handler: suspend WebsocketSession.() -> Unit
) {
    val sessionManager by closestDI { this.application }.instance<Ocpp16SessionManager>()

    val chargePointId = call.parameters["chargePointId"]

    if (chargePointId != null && chargePointId.isNotEmpty()) {
        val session = WebsocketSession(chargePointId, this)
        sessionManager.registerSession(session)
        log.debug("Created session [$chargePointId]")

        try {
            runBlocking {
                handler(session)
            }
        } catch (_: ClosedReceiveChannelException) {
            log.debug("Closed connection [$chargePointId]")
        }

        session.closeSession()
        sessionManager.unregisterSession(chargePointId)
    } else {
        call.response.status(HttpStatusCode.NotFound)
    }
}

class WebsocketSession(
    override val chargePointId: String,
    private val session: DefaultWebSocketServerSession
) : Ocpp16Session {

    companion object {
        private val log = LoggerFactory.getLogger(WebsocketSession::class.java)

        private const val REQUEST_TIMEOUT_MS = 10000L
        private const val RESPONSE_TIMEOUT_MS = 10000L
    }

    private val messageReceiver by closestDI { session.application }.instance<Ocpp16MessageReceiver>()

    private val outgoingMessagesLock = Mutex()

    private val pendingIncomingCallResponses =
        ConcurrentHashMap<String, Pair<CompletableDeferred<Either<Ocpp16Error, Ocpp16IncomingResponse>>, Job>>()

    suspend fun handleIncomingMessages() {
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    log.trace("Received message '$text' [$chargePointId]")

                    WebsocketMessageDeserializer.deserialize(text) { it }
                        .fold(
                            {
                                sendText(it.toCallError().serialize())
                            },
                            { incomingMessage ->
                                val uniqueId = incomingMessage.uniqueId
                                when (incomingMessage) {
                                    is IncomingCall -> {
                                        log.debug("Handling Call [$chargePointId/${uniqueId}]")

                                        handleIncomingCall(incomingMessage)
                                    }
                                    is IncomingCallResult -> {
                                        log.debug("Handling CallResult [$chargePointId/${uniqueId}]")

                                        handleIncomingCallResponse(
                                            uniqueId,
                                            incomingMessage.payload.right()
                                        )
                                    }
                                    is CallError -> {
                                        log.debug("Handling CallError [$chargePointId/${uniqueId}]")

                                        handleIncomingCallResponse(
                                            uniqueId,
                                            incomingMessage.toOcpp16Error().left()
                                        )
                                    }
                                    else -> {
                                        log.error("Ignoring unknown message type [$chargePointId]")
                                    }
                                }
                            }
                        )
                }
                else -> {
                    log.error("Illegal frame content, ignoring message [$chargePointId]")
                    session.close(CloseReason(CloseReason.Codes.NORMAL, "Client sent illegal frame content"))
                }
            }
        }
    }

    suspend fun sendOutgoingCall(message: Ocpp16OutgoingRequest):
            Deferred<Either<Ocpp16Error, Ocpp16IncomingResponse>> {

        val uniqueId = UUID.randomUUID().toString()
        val promise: CompletableDeferred<Either<Ocpp16Error, Ocpp16IncomingResponse>> = CompletableDeferred()

        withContext(Dispatchers.Default) {
            launch {
                // Send message in background
                withTimeout(REQUEST_TIMEOUT_MS) {
                    outgoingMessagesLock.lock()
                    val call = OutgoingCall(uniqueId, message.getActionName(), message)
                    sendText(call.serialize())
                }
            }.invokeOnCompletion { error ->
                // Register listener which is called when message sending is completed
                error
                    ?.let { // Error
                        outgoingMessagesLock.unlock()
                        promise.complete(GenericError("Could not send Call to client").left())
                        log.error("Could not send Call to client [$chargePointId]")
                    }
                    ?: let { // Success
                        // Create response timeout handler
                        val receiveMessageTimeoutTask = launch {
                            withTimeout(RESPONSE_TIMEOUT_MS) {
                                outgoingMessagesLock.unlock()
                                pendingIncomingCallResponses.remove(uniqueId)
                            }
                        }

                        // Register for incoming response
                        pendingIncomingCallResponses.put(uniqueId, promise to receiveMessageTimeoutTask)
                    }
            }
        }

        return promise
    }

    fun closeSession() {
        pendingIncomingCallResponses.forEach {
            // Cancel pending timeout jobs
            val job = it.value.second
            job.cancel()

            // Resolve all pending promises with error
            val promise = it.value.first
            promise.complete(GenericError("Session was closed").left())
        }

        log.debug("Session closed  [$chargePointId]")
    }

    private suspend fun handleIncomingCall(message: IncomingCall) {
        val callResponse = messageReceiver.handleMessage(this@WebsocketSession, message.payload)
            .fold(
                {
                    it.toCallError().serialize()
                },
                {
                    OutgoingCallResult(message.uniqueId, it).serialize()
                }
            )

        sendText(callResponse)
    }

    private fun handleIncomingCallResponse(uniqueId: String, result: Either<Ocpp16Error, Ocpp16IncomingResponse>) {
        pendingIncomingCallResponses[uniqueId]
            ?.let {
                outgoingMessagesLock.unlock()
                pendingIncomingCallResponses.remove(uniqueId)

                val timeoutTask = it.second
                timeoutTask.cancel()

                val promise = it.first
                promise.complete(result)
            }
            ?: let {
                log.warn("Ingoring incoming CallResult/CallError as no one is waiting for it [$chargePointId]")
            }
    }

    private suspend fun sendText(text: String) {
        session.outgoing.send(Frame.Text(text))
    }
}

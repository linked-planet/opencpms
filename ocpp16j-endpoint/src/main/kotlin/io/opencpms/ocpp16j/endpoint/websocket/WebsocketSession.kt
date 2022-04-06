/**
 * OpenCPMS
 * Copyright (C) 2022 linked-planet GmbH (info@linked-planet.com).
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

import arrow.core.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import io.opencpms.ocpp16.protocol.*
import io.opencpms.ocpp16.protocol.message.*
import io.opencpms.ocpp16j.endpoint.json.*
import io.opencpms.ocpp16j.endpoint.protocol.*
import io.opencpms.ocpp16j.endpoint.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.LoggerFactory
import pl.jutupe.ktor_rabbitmq.publish
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    private val outgoingMessagesLock = Mutex()

    private val pendingIncomingCallResponses =
        ConcurrentHashMap<String, Pair<CompletableDeferred<Either<Ocpp16Error, Ocpp16IncomingResponse>>, Job>>()

    suspend fun ApplicationCall.handleIncomingMessages() {
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

    private suspend fun ApplicationCall.handleIncomingCall(message: IncomingCall) {
        publish(
            "boot_notification",
            "boot_notification",
            null,
            (message.payload as BootNotificationRequest)
        )
        // TODO response from rabbit
        val response = BootNotificationResponse(BootNotificationResponse.Status.Accepted, OffsetDateTime.now(), 10L)
        val callResponse = OutgoingCallResult(message.uniqueId, response).serialize()
//        val callResponse = messageReceiver.handleMessage(this@WebsocketSession, message.payload)
//            .fold(
//                {
//                    it.toCallError().serialize()
//                },
//                {
//                    OutgoingCallResult(message.uniqueId, it).serialize()
//                }
//            )

        sendText(callResponse)
    }

    private fun handleIncomingCallResponse(
        uniqueId: String,
        result: Either<Ocpp16Error, Ocpp16IncomingResponse>
    ) {
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
                log.warn("Ingoring incoming CallResult/CallError as nobody is waiting for it [$chargePointId]")
            }
    }

    private suspend fun sendText(text: String) {
        session.outgoing.send(Frame.Text(text))
    }
}

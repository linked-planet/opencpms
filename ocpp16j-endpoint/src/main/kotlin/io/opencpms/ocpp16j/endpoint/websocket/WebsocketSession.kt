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
import arrow.core.computations.either
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.application
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingMessage
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16.service.session.Ocpp16Session
import io.opencpms.ocpp16.service.session.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.json.IncomingCallDeserializer
import io.opencpms.ocpp16j.endpoint.protocol.CallError
import io.opencpms.ocpp16j.endpoint.protocol.CallResult
import io.opencpms.ocpp16j.endpoint.protocol.toCallError
import io.opencpms.ocpp16j.endpoint.util.GSON
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
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
            // Only one message at the time is supported in OCPP1.6
            runBlocking {
                handler(session)
            }

        } catch (_: ClosedReceiveChannelException) {
            log.debug("Closed connection [$chargePointId]")
        }

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
    }

    suspend fun ocpp16Call(
        handler: (Ocpp16Session, Ocpp16IncomingMessage) -> Either<Ocpp16Error, Ocpp16OutgoingMessage>
    ) {
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    log.trace("Received message '$text' [$chargePointId]")

                    // TODO: improve
                    val that = this
                    val callResponseEither: Either<CallError, CallResult> = either {
                        val call = IncomingCallDeserializer.deserialize(text).bind()
                        val uniqueId = call.uniqueId
                        val incomingMessage = call.payload

                        handler(that, incomingMessage)
                            .map { CallResult(uniqueId, it) }
                            .mapLeft { it.toCallError(uniqueId) }.bind()
                    }

                    session.outgoing.send(Frame.Text(GSON.toJson(callResponseEither.fold({ it }, { it }))))
                }
                else -> {
                    log.error("Dropping unknown message type [$chargePointId]")
                    session.close(CloseReason(CloseReason.Codes.NORMAL, "Client sent unknown message type"))
                }
            }
        }
    }
}

package io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.Either
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import io.opencpms.ocpp16.protocol.*
import io.opencpms.ocpp16.service.*
import io.opencpms.ocpp16j.endpoint.protocol.*
import io.opencpms.ocpp16j.endpoint.util.GSON
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

class WebsocketSession(
    override val chargePointId: String,
    private val session: DefaultWebSocketServerSession
) : Ocpp16Session {

    suspend fun ocpp16Call(
        handler: (Ocpp16Session, Ocpp16IncomingMessage) -> Either<Error, Ocpp16OutgoingMessage>
    ) {
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    // TODO: json validation (IllegalArgumentException on construction)
                    val call = GSON.fromJson(text, Call::class.java)
                    val incomingMessage = call.payload
                    val uniqueId = call.uniqueId

                    val callResponse = handler(this, incomingMessage)
                        .fold(
                            ifLeft = { error ->
                                val description = error.localizedMessage
                                val details = error.stackTraceToString()
                                CallError(uniqueId, Ocpp16ErrorCode.GenericError, description, details)
                            },
                            ifRight = { outgoingMessage ->
                                CallResult(uniqueId, outgoingMessage)
                            }
                        )

                    session.outgoing.send(Frame.Text(GSON.toJson(callResponse)))
                }
                else -> {
                    // TODO: else
                    session.close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }
    }
}

fun DefaultWebSocketServerSession.ocpp16Session(
    handler: suspend WebsocketSession.() -> Unit
) {
    val sessionManager by closestDI { this.application }.instance<Ocpp16SessionManager>()

    val chargePointId = call.parameters["chargePointId"]

    if (chargePointId != null && chargePointId.isNotEmpty()) {
        val session = WebsocketSession(chargePointId, this)
        sessionManager.registerSession(session)

        try {
            // Only one message at the time is supported in OCPP1.6
            runBlocking {
                handler(session)
            }
        } catch (e: ClosedReceiveChannelException) {
            // TODO: handle onClose
        }

        sessionManager.unregisterSession(chargePointId)
    } else {
        // TODO: correct error handling
        call.response.status(HttpStatusCode.NotFound)
    }
}

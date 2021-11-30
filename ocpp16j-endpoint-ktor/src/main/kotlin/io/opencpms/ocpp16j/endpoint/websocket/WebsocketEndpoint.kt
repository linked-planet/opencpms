package io.opencpms.ocpp16j.endpoint.websocket

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16.service.*
import org.kodein.di.*
import org.kodein.di.ktor.*
import java.time.Duration

private const val OCPP16_WEBSOCKET_PROTOCOL_HEADER_VALUE = "ocpp1.6"

fun Application.configureSockets(context: DI) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(DefaultHeaders)
    install(CallLogging)

    di {
        extend(context)
    }

    routing {
        ocpp16AuthorizedChargePoint {
            webSocket("/ocpp/16/{chargePointId}", OCPP16_WEBSOCKET_PROTOCOL_HEADER_VALUE) {
                ocpp16Session {
                    ocpp16Call { session: Ocpp16Session, message: Ocpp16IncomingMessage ->
                        val incomingMessageService by closestDI().instance<Ocpp16IncomingMessageService>()
                        incomingMessageService.handleMessage(session, message)
                    }
                }
            }
        }
    }
}


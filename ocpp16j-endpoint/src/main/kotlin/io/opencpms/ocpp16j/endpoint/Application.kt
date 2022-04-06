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
package io.opencpms.ocpp16j.endpoint

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.opencpms.ktor.rabbitmq.*
import io.opencpms.ocpp16.protocol.ocpp16JsonMapper
import io.opencpms.ocpp16j.endpoint.auth.Ocpp16AuthServiceImpl
import io.opencpms.ocpp16j.endpoint.session.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.websocket.*
import org.kodein.di.*
import org.kodein.di.ktor.di
import java.time.Duration

private const val OCPP16_WEBSOCKET_PROTOCOL_HEADER_VALUE = "ocpp1.6"
private const val WEBSOCKET_PING_PERIOD_SECONDS = 15L
private const val WEBSOCKET_TIMEOUT_SECONDS = 15L


fun Application.main() {
    main(DI {
        bind { singleton { environment.config } }
        bind { singleton { Ocpp16AuthServiceImpl() } }
        bind { singleton { Ocpp16SessionManager() } }
    })
}

fun Application.main(context: DI) {
    di {
        extend(context)
    }

    install(DefaultHeaders)
    install(CallLogging)

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(WEBSOCKET_PING_PERIOD_SECONDS)
        timeout = Duration.ofSeconds(WEBSOCKET_TIMEOUT_SECONDS)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(RabbitMQ) {
        uri = "amqp://guest:guest@localhost:5672"
        connectionName = "ocpp16j-endpoint"

        enableLogging()

        serialize { ocpp16JsonMapper.writeValueAsBytes(it) }
        deserialize { bytes, type -> ocpp16JsonMapper.readValue(bytes, type.javaObjectType) }
    }

    rabbitMq {
        newChannel("ocpp16_request_publish") {
            exchangeDeclare("ocpp16_request", "topic", true)
            queueDeclare("ocpp16_request", true, false, false, emptyMap())
            queueBind("ocpp16_request", "ocpp16_request", "ocpp16_request")
        }
    }

    routing {
        ocpp16AuthorizedChargePoint {
            webSocket("/ocpp/16/{chargePointId}", OCPP16_WEBSOCKET_PROTOCOL_HEADER_VALUE) {
                ocpp16Session {
                    application.handleIncomingMessages()
                }
            }
        }
    }

}

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
package io.opencpms.cpms

import io.ktor.application.*
import io.ktor.features.*
import io.opencpms.ktor.rabbitmq.*
import io.opencpms.ocpp16.protocol.*
import io.opencpms.ocpp16.protocol.message.BootNotificationRequest
import org.kodein.di.*
import org.kodein.di.ktor.di


fun Application.main() {
    main(DI {
        bind { singleton { environment.config } }
    })
}

fun Application.main(context: DI) {
    di {
        extend(context)
    }

    install(DefaultHeaders)
    install(CallLogging)

    install(RabbitMQ) {
        uri = "amqp://guest:guest@localhost:5672"
        connectionName = "cpms"

        enableLogging()

        serialize { ocpp16JsonMapper.writeValueAsBytes(it) }
        deserialize { bytes, type -> ocpp16JsonMapper.readValue(bytes, type.javaObjectType) }
    }

    rabbitMq {
        newChannel("ocpp16_request_consume") {
            exchangeDeclare("ocpp16_request", "topic", true)
            queueDeclare("ocpp16_request", true, false, false, emptyMap())
            queueBind("ocpp16_request", "ocpp16_request", "ocpp16_request")
            consume<Ocpp16IncomingRequestEnvelope>(this, "ocpp16_request") { body ->
                println("Consumed message $body")
                when (body.payload) {
                    is BootNotificationRequest -> println("It was a boot notification! Hurray!")
                }
            }
        }
    }
}

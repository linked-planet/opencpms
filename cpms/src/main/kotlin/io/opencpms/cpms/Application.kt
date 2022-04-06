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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.features.*
import io.opencpms.ocpp16.protocol.message.BootNotificationRequest
import org.kodein.di.*
import org.kodein.di.ktor.di
import pl.jutupe.ktor_rabbitmq.*


fun Application.main() {
    main(DI {
        bind { singleton { environment.config } }
    })
}

fun Application.main(context: DI) {
    install(DefaultHeaders)
    install(CallLogging)

    di {
        extend(context)
    }

    rabbit()

    rabbitConsumer {
        consume<BootNotificationRequest>("boot_notification") { body ->
            println("Consumed message $body")
        }
    }
}

private fun Application.rabbit() {
    install(RabbitMQ) {
        uri = "amqp://guest:guest@localhost:5672"
        connectionName = "cpms"

        enableLogging()

        serialize { jacksonObjectMapper().writeValueAsBytes(it) }
        deserialize { bytes, type -> jacksonObjectMapper().readValue(bytes, type.javaObjectType) }

        initialize {
            exchangeDeclare(
                "boot_notification",
                "topic",
                false
            )
            queueDeclare(
                "boot_notification",
                false,
                false,
                false,
                emptyMap()
            )
            queueBind(
                "boot_notification",
                "boot_notification",
                "boot_notification"
            )
        }
    }
}

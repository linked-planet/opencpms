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
package io.opencpms.ktor.rabbitmq

import io.ktor.application.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

@ContextDsl
fun Application.rabbitMq(configuration: RabbitMQInstance.() -> Unit): RabbitMQInstance =
    feature(RabbitMQ).apply(configuration)

class RabbitMQ {

    companion object Feature : ApplicationFeature<Application, RabbitMQConfiguration, RabbitMQInstance> {

        override val key: AttributeKey<RabbitMQInstance> = AttributeKey("RabbitMQ")

        override fun install(
            pipeline: Application,
            configure: RabbitMQConfiguration.() -> Unit
        ): RabbitMQInstance {
            val configuration = RabbitMQConfiguration.create().apply(configure)
            val rabbit = configuration.rabbitMQInstance ?: RabbitMQInstance(configuration)
            return rabbit.apply {
                pipeline.attributes.put(key, this)
                logger?.info("RabbitMQ initialized")
            }
        }

    }

}

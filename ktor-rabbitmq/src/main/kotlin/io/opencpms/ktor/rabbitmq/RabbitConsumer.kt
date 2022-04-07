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

import com.rabbitmq.client.*
import io.ktor.util.pipeline.*

@ContextDsl
inline fun <reified T> RabbitMQInstance.consume(
    channel: Channel,
    queue: String,
    autoAck: Boolean = true,
    basicQos: Int? = null,
    crossinline rabbitDeliverCallback: ConsumerScope.(body: T) -> Unit
) {
    basicQos?.let { channel.basicQos(it) }
    channel.basicConsume(
        queue,
        autoAck,
        { consumerTag, message ->
            runCatching {
                val mappedEntity = deserialize<T>(message.body)
                val scope = ConsumerScope(channel, message)
                rabbitDeliverCallback.invoke(scope, mappedEntity)
            }.getOrElse { throwable ->
                logger?.error(
                    "DeliverCallback error: (" +
                            "messageId = ${message.properties.messageId}, " +
                            "consumerTag = $consumerTag)",
                    throwable,
                )
            }
        },
        { consumerTag ->
            logger?.error("Consume cancelled: (consumerTag = $consumerTag)")
        }
    )
}

class ConsumerScope(
    private val channel: Channel,
    private val message: Delivery
) {

    fun ack(multiple: Boolean = false) {
        channel.basicAck(message.envelope.deliveryTag, multiple)
    }

    fun nack(multiple: Boolean = false, requeue: Boolean = false) {
        channel.basicNack(message.envelope.deliveryTag, multiple, requeue)
    }

    fun reject(requeue: Boolean = false) {
        channel.basicReject(message.envelope.deliveryTag, requeue)
    }

}

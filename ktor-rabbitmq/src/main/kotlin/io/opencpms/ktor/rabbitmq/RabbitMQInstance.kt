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
import org.slf4j.Logger
import java.util.*

class RabbitMQInstance(
    val configuration: RabbitMQConfiguration
) {

    val logger: Logger? = configuration.logger

    private val connectionFactory =
        ConnectionFactory().apply {
            setUri(configuration.uri)
        }

    private val connection = connectionFactory.newConnection(configuration.connectionName)

    private val channels = Collections.synchronizedMap<String, Channel>(LinkedHashMap())

    fun newChannel(name: String, block: Channel.() -> Unit): Channel {
        val channel = connection.createChannel()
        channels += name to channel
        block.invoke(channel)
        return channel
    }

    fun withChannel(name: String, block: Channel.() -> Unit) {
        channels[name]
            ?.let { block.invoke(it) }
            ?: logger?.error("Channel $name not found")
    }

    inline fun <reified T> deserialize(bytes: ByteArray): T =
        configuration.deserializeBlock.invoke(bytes, T::class) as T

    fun <T> serialize(body: T): ByteArray =
        configuration.serializeBlock.invoke(body!!)

}

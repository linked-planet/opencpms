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
import org.slf4j.Logger
import kotlin.reflect.KClass

class RabbitMQConfiguration private constructor() {

    lateinit var uri: String
    var rabbitMQInstance: RabbitMQInstance? = null
    var connectionName: String? = null
    var logger: Logger? = null
        private set

    lateinit var serializeBlock: (Any) -> ByteArray
    lateinit var deserializeBlock: (ByteArray, KClass<*>) -> Any

    /**
     * Enables logging by passing [Application.log] into [RabbitMQ]
     */
    fun Application.enableLogging() {
        logger = log
    }

    /**
     * @param [block] used for message body serialization.
     */
    fun serialize(block: (Any) -> ByteArray) {
        serializeBlock = block
    }

    /**
     * @param [block] used for message body deserialization.
     */
    fun deserialize(block: (ByteArray, KClass<*>) -> Any) {
        deserializeBlock = block
    }

    companion object {
        fun create(): RabbitMQConfiguration {
            return RabbitMQConfiguration()
        }
    }

}

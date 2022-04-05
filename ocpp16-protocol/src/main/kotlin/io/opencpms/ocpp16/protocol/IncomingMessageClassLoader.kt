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
package io.opencpms.ocpp16.protocol

import io.opencpms.ocpp16.protocol.message.BootNotificationRequest

object IncomingMessageClassLoader {

    fun loadOcpp16IncomingRequestClass(clazz: String): Class<out Ocpp16IncomingRequest> = when (clazz) {
        "BootNotification" -> BootNotificationRequest::class.java
        else -> throw ClassNotFoundException("No class $clazz found")
    }

    fun loadOcpp16IncomingResponseClass(clazz: String): Class<Ocpp16IncomingResponse> {
        throw ClassNotFoundException("No class $clazz found")
    }
}

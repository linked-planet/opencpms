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
package io.opencpms.ocpp16.protocol.message

import io.opencpms.ocpp16.protocol.Ocpp16OutgoingResponse
import java.time.OffsetDateTime

data class BootNotificationResponse(
    val status: Status,
    val currentTime: OffsetDateTime,
    val interval: Long
) : Ocpp16OutgoingResponse {

    enum class Status {
        Accepted,
        Pending,
        Rejected
    }

}

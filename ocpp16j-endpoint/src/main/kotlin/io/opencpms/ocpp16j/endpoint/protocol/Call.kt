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
package io.opencpms.ocpp16j.endpoint.protocol

import io.opencpms.ocpp16.protocol.Ocpp16IncomingRequest
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingRequest

const val CALL_MESSAGE_TYPE_ID = 2

data class IncomingCall(
    override val uniqueId: String,
    val actionName: String,
    val payload: Ocpp16IncomingRequest,
    val messageTypeId: Int
) : WebsocketMessage {
    companion object
}

data class OutgoingCall(
    override val uniqueId: String,
    val actionName: String,
    val payload: Ocpp16OutgoingRequest,
) : WebsocketMessage {
    val messageTypeId = CALL_RESULT_MESSAGE_TYPE_ID
}

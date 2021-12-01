/**
 * OpenCPMS
 * Copyright (C) 2021 linked-planet GmbH (info@linked-planet.com).
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

import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingMessage

const val CALL_MESSAGE_TYPE_ID = 2
const val CALL_RESULT_MESSAGE_TYPE_ID = 3
const val CALL_ERROR_MESSAGE_TYPE_ID = 4

data class Call(
    val uniqueId: String,
    val actionName: String,
    val payload: Ocpp16IncomingMessage
) {
    val messageTypeId = CALL_MESSAGE_TYPE_ID
}

interface CallResponse

data class CallResult(
    val uniqueId: String,
    val payload: Ocpp16OutgoingMessage
) : CallResponse {
    val messageTypeId = CALL_RESULT_MESSAGE_TYPE_ID
}

data class CallError(
    val uniqueId: String,
    val errorCode: Ocpp16ErrorCode,
    val errorDescription: String = "",
    val errorDetails: String
) : CallResponse {
    val messageTypeId: Int = CALL_ERROR_MESSAGE_TYPE_ID
}

enum class Ocpp16ErrorCode {
    NotImplemented,
    NotSupported,
    InternalError,
    ProtocolError,
    SecurityError,
    FormationViolation,
    PropertyConstraintViolation,
    OccurenceConstraintViolation,
    TypeConstraintViolation,
    GenericError
}

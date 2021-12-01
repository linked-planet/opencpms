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

import io.opencpms.ocpp16.protocol.*

data class Call(
    val uniqueId: String,
    val actionName: String,
    val payload: Ocpp16IncomingMessage
) {
    val messageTypeId = 2
}

interface CallResponse

data class CallResult(
    val uniqueId: String,
    val payload: Ocpp16OutgoingMessage
) : CallResponse {
    val messageTypeId = 3
}

data class CallError(
    val uniqueId: String,
    val errorCode: Ocpp16ErrorCode,
    val errorDescription: String = "",
    val errorDetails: String
) : CallResponse {
    val messageTypeId: Int = 4
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
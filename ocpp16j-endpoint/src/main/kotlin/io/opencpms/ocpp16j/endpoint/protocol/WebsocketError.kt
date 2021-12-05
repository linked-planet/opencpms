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

import io.opencpms.ocpp16.service.NotSupportedError
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16.service.ProtocolError
import io.opencpms.ocpp16.service.SecurityError
import io.opencpms.ocpp16.service.UNKNOWN_UNIQUE_ID

class NotImplemented(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Requested Action is not known by receiver",
    details
)

class FormationViolation(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
    details
)

class PropertyConstraintViolation(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Payload is syntactically correct but at least one field contains an invalid value",
    details
)

class OccurenceConstraintViolation(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Payload for Action is syntactically correct but at least one of the fields violates" +
            " occurence constraints",
    details
)

class TypeConstraintViolation(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Payload for Action is syntactically correct but at least one of the fields violates" +
            " data type constraints (e.g. “somestring”: 12)",
    details
)

class GenericError(reason: String, uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) :
    Ocpp16Error(uniqueId, reason, details)

fun Ocpp16Error.toCallError(): CallError {
    val errorCode = when (this) {
        is NotSupportedError -> Ocpp16ErrorCode.NotSupported
        is io.opencpms.ocpp16.service.InternalError -> Ocpp16ErrorCode.InternalError
        is ProtocolError -> Ocpp16ErrorCode.ProtocolError
        is SecurityError -> Ocpp16ErrorCode.SecurityError
        is FormationViolation -> Ocpp16ErrorCode.FormationViolation
        is PropertyConstraintViolation -> Ocpp16ErrorCode.PropertyConstraintViolation
        is OccurenceConstraintViolation -> Ocpp16ErrorCode.OccurenceConstraintViolation
        is TypeConstraintViolation -> Ocpp16ErrorCode.TypeConstraintViolation
        is GenericError -> Ocpp16ErrorCode.GenericError
        else -> {
            Ocpp16ErrorCode.GenericError
        }
    }
    return CallError(uniqueId, errorCode, reason, details)
}

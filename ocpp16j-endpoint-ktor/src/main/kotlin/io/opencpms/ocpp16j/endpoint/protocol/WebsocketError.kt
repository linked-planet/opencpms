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

class FormationViolation(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class PropertyConstraintViolation(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class OccurenceConstraintViolation(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class TypeConstraintViolation(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class GenericError(reason: String, details: String? = null) : Ocpp16Error(reason, details)

fun Ocpp16Error.toCallError(): CallError = toCallError("UNKNOWN")

fun Ocpp16Error.toCallError(uniqueId: String): CallError {
    val errorCode = when (this) {
        is NotSupportedError -> Ocpp16ErrorCode.NotSupported
        is io.opencpms.ocpp16.service.InternalError -> Ocpp16ErrorCode.InternalError
        is ProtocolError -> Ocpp16ErrorCode.ProtocolError
        is SecurityError -> Ocpp16ErrorCode.SecurityError
        is FormationViolation -> Ocpp16ErrorCode.FormationViolation
        is PropertyConstraintViolation -> Ocpp16ErrorCode.PropertyConstraintViolation
        is OccurenceConstraintViolation -> Ocpp16ErrorCode.OccurenceConstraintViolation
        is TypeConstraintViolation -> Ocpp16ErrorCode.TypeConstraintViolation
        else -> Ocpp16ErrorCode.GenericError
    }
    return CallError(uniqueId, errorCode, reason, details)
}

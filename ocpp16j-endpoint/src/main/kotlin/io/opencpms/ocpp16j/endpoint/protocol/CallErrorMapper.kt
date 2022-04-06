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

import io.opencpms.ocpp16.protocol.*

fun Ocpp16Error.toCallError(): CallError {
    val errorCode = when (this) {
        is NotSupportedError -> Ocpp16ErrorCode.NotSupported
        is InternalError -> Ocpp16ErrorCode.InternalError
        is ProtocolError -> Ocpp16ErrorCode.ProtocolError
        is SecurityError -> Ocpp16ErrorCode.SecurityError
        is FormationViolation -> Ocpp16ErrorCode.FormationViolation
        is PropertyConstraintViolation -> Ocpp16ErrorCode.PropertyConstraintViolation
        is OccurrenceConstraintViolation -> Ocpp16ErrorCode.OccurenceConstraintViolation
        is TypeConstraintViolation -> Ocpp16ErrorCode.TypeConstraintViolation
        is GenericError -> Ocpp16ErrorCode.GenericError
        else -> {
            Ocpp16ErrorCode.GenericError
        }
    }
    return CallError(uniqueId, errorCode, reason, details)
}

fun CallError.toOcpp16Error(): Ocpp16Error {
    return when (this.errorCode) {
        Ocpp16ErrorCode.NotSupported -> NotSupportedError(uniqueId, errorDetails)
        Ocpp16ErrorCode.InternalError -> InternalError(uniqueId, errorDetails)
        Ocpp16ErrorCode.ProtocolError -> ProtocolError(uniqueId, errorDetails)
        Ocpp16ErrorCode.SecurityError -> SecurityError(uniqueId, errorDetails)
        Ocpp16ErrorCode.FormationViolation -> FormationViolation(uniqueId, errorDetails)
        Ocpp16ErrorCode.PropertyConstraintViolation -> PropertyConstraintViolation(uniqueId, errorDetails)
        Ocpp16ErrorCode.OccurenceConstraintViolation -> OccurrenceConstraintViolation(uniqueId, errorDetails)
        Ocpp16ErrorCode.TypeConstraintViolation -> TypeConstraintViolation(uniqueId, errorDetails)
        Ocpp16ErrorCode.GenericError -> GenericError(errorDescription, uniqueId, errorDetails)
        else -> {
            GenericError(errorDescription, uniqueId, errorDetails)
        }
    }
}

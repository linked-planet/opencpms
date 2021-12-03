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
package io.opencpms.ocpp16j.endpoint.json

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16j.endpoint.protocol.Call
import io.opencpms.ocpp16j.endpoint.protocol.FormationViolation
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.PropertyConstraintViolation
import io.opencpms.ocpp16j.endpoint.util.GSON
import io.opencpms.ocpp16j.endpoint.util.JACKSON

private const val OCPP16_PACKAGE_NAME = "io.opencpms.ocpp16.protocol.message"

private const val OCPP16_CALL_CLASS_SUFFIX = "Request"

/**
 * Allows parsing the payload of incoming calls.
 */
@Suppress("TooGenericExceptionCaught")
data class RawCall(
    val uniqueId: String,
    val actionName: String,
    val messageTypeId: Int,
    val payload: String
) {
    companion object {
        fun fromJson(json: String): Either<Ocpp16Error, RawCall> = try {
            GSON.fromJson(json, RawCall::class.java).right()
        } catch (_: Exception) {
            GenericError("Could not parse call, invalid [json-]format").left()
        }
    }

    fun toCall(): Either<Ocpp16Error, Call> = try {
        // In a call we are working with requests only
        val actionClass = Class.forName("$OCPP16_PACKAGE_NAME.${actionName}$OCPP16_CALL_CLASS_SUFFIX")

        // Use special jackson parser for action as gson doesn't call init block during class initialization
        val action = JACKSON.readValue(payload, actionClass) as Ocpp16IncomingMessage

        Call(uniqueId, actionName, action, messageTypeId).right()
    } catch (e: Exception) {
        val error = when (e) {
            is ValueInstantiationException -> {
                PropertyConstraintViolation()
            }
            else -> {
                FormationViolation()
            }
        }
        error.left()
    }
}

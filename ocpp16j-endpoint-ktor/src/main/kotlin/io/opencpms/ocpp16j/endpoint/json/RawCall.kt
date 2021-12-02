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
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16j.endpoint.protocol.Call
import io.opencpms.ocpp16j.endpoint.protocol.FormationViolation
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.util.GSON

private const val OCPP16_PACKAGE_NAME = "io.opencpms.ocpp16.protocol.message"

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
        val actionClass = Class.forName("$OCPP16_PACKAGE_NAME.${actionName}")
        val action = GSON.fromJson(payload, actionClass) as Ocpp16IncomingMessage
        Call(uniqueId, actionName, action, messageTypeId).right()
    } catch (_: Exception) {
        FormationViolation(
            "Payload for Action is syntactically incorrect or not conform the PDU structure for Action"
        ).left()
    }
}

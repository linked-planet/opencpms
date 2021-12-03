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
import arrow.core.computations.either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.protocol.Call
import io.opencpms.ocpp16j.endpoint.protocol.CallError
import io.opencpms.ocpp16j.endpoint.protocol.FormationViolation
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.PropertyConstraintViolation
import io.opencpms.ocpp16j.endpoint.protocol.toCallError
import io.opencpms.ocpp16j.endpoint.util.JACKSON

private const val OCPP16_PACKAGE_NAME = "io.opencpms.ocpp16.protocol.message"

private const val OCPP16_CALL_CLASS_SUFFIX = "Request"

@Suppress("MagicNumber", "TooGenericExceptionCaught")
object IncomingCallDeserializer {

    suspend fun deserialize(json: String): Either<CallError, Call> = try {
        val jsonTree = JsonParser.parseString(json).asJsonArray
        deserialize(jsonTree)
    } catch (_: Exception) {
        GenericError("Could not parse call, invalid [json-]format").toCallError().left()
    }

    suspend fun deserialize(json: JsonArray): Either<CallError, Call> = either {
        val rawCall = parseRawCall(json).bind()
        parseAction(rawCall).bind()
    }

    private fun parseRawCall(jsonArray: JsonArray): Either<CallError, RawCall> {
        var uniqueId: String? = null
        return try {
            require(jsonArray.size() == 4)

            val messageTypeId = jsonArray.get(0).asInt
            require(messageTypeId == CALL_MESSAGE_TYPE_ID)

            uniqueId = jsonArray.get(1).asString
            val actionName = jsonArray.get(2).asString
            val payload = jsonArray.get(3).asJsonObject
            RawCall(messageTypeId, uniqueId, actionName, payload.toString()).right()
        } catch (_: Exception) {
            GenericError("Could not parse call, invalid [json-]format").toCallError().left()
        }
    }

    private fun parseAction(raw: RawCall): Either<CallError, Call> = try {
        val actionName = raw.actionName

        // In a call we are working with requests only
        val actionClass = Class.forName("$OCPP16_PACKAGE_NAME.${actionName}$OCPP16_CALL_CLASS_SUFFIX")

        // Use special jackson parser for action as gson doesn't call init block during class initialization
        val action = JACKSON.readValue(raw.payload, actionClass) as Ocpp16IncomingMessage

        Call(raw.uniqueId, actionName, action, raw.messageTypeId).right()
    } catch (e: Exception) {
        val error = when (e) {
            is ValueInstantiationException -> {
                PropertyConstraintViolation()
            }
            else -> {
                FormationViolation()
            }
        }
        error.toCallError(raw.uniqueId).left()
    }

    private data class RawCall(
        val messageTypeId: Int,
        val uniqueId: String,
        val actionName: String,
        val payload: String
    )
}

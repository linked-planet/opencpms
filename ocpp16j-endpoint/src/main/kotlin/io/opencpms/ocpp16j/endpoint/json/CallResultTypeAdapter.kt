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
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.protocol.FormationViolation
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.IncomingCallResult
import io.opencpms.ocpp16j.endpoint.protocol.OutgoingCallResult
import io.opencpms.ocpp16j.endpoint.protocol.PropertyConstraintViolation
import io.opencpms.ocpp16j.endpoint.util.GSON
import io.opencpms.ocpp16j.endpoint.util.JACKSON

private const val CALL_RESULT_ENTRIES = 3
private const val MESSAGE_TYPE_ID_INDEX = 0
private const val UNIQUE_ID_INDEX = 1
private const val PAYLOAD_INDEX = 2

@Suppress("TooGenericExceptionCaught")
object CallResultTypeAdapter {

    fun serialize(callResult: OutgoingCallResult): String {
        val payload = GSON.toJsonTree(callResult.payload)

        val jsonArray = JsonArray()
        jsonArray.add(callResult.messageTypeId)
        jsonArray.add(callResult.uniqueId)
        jsonArray.add(payload)

        return GSON.toJson(jsonArray)
    }

    suspend fun deserialize(className: String, json: String): Either<Ocpp16Error, IncomingCallResult> = try {
        val jsonTree = JsonParser.parseString(json).asJsonArray
        deserialize(className, jsonTree)
    } catch (_: Exception) {
        GenericError("Could not parse CallResult, invalid [json-]format").left()
    }

    private suspend fun deserialize(className: String, json: JsonArray): Either<Ocpp16Error, IncomingCallResult> =
        either {
            val rawCall = parseRawCallResult(json).bind()
            parseAction(className, rawCall).bind()
        }

    private fun parseRawCallResult(jsonArray: JsonArray): Either<Ocpp16Error, RawIncomingCallResult> {
        return try {
            require(jsonArray.size() == CALL_RESULT_ENTRIES)

            val messageTypeId = jsonArray.get(MESSAGE_TYPE_ID_INDEX).asInt
            require(messageTypeId == CALL_MESSAGE_TYPE_ID)

            val uniqueId = jsonArray.get(UNIQUE_ID_INDEX).asString

            val payload = jsonArray.get(PAYLOAD_INDEX).asJsonObject

            RawIncomingCallResult(messageTypeId, uniqueId, payload.toString()).right()
        } catch (_: Exception) {
            GenericError("Could not parse CallResult, invalid [json-]format").left()
        }
    }

    private fun parseAction(
        className: String,
        raw: RawIncomingCallResult
    ): Either<Ocpp16Error, IncomingCallResult> = try {
        // In a call we are working with requests only
        val actionClass = Class.forName("$OCPP16_PACKAGE_NAME.${className}$OCPP16_CALL_RESULT_CLASS_SUFFIX")

        // Use special jackson parser for action as gson doesn't call init block during class initialization
        val action = JACKSON.readValue(raw.payload, actionClass) as Ocpp16IncomingMessage

        IncomingCallResult(raw.uniqueId, action, raw.messageTypeId).right()
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

    private data class RawIncomingCallResult(
        val messageTypeId: Int,
        val uniqueId: String,
        val payload: String
    )
}

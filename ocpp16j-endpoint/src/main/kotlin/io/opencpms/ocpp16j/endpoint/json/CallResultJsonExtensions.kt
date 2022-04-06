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
package io.opencpms.ocpp16j.endpoint.json

import arrow.core.*
import arrow.core.computations.either
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.google.gson.JsonArray
import io.opencpms.ocpp16.protocol.*
import io.opencpms.ocpp16j.endpoint.protocol.*
import io.opencpms.ocpp16j.endpoint.util.GSON

private const val MESSAGE_TYPE_ID_INDEX = 0
private const val UNIQUE_ID_INDEX = 1
private const val PAYLOAD_INDEX = 2

fun OutgoingCallResult.serialize(): String {
    val payload = GSON.toJsonTree(payload)

    val jsonArray = JsonArray()
    jsonArray.add(messageTypeId)
    jsonArray.add(uniqueId)
    jsonArray.add(payload)

    return GSON.toJson(jsonArray)
}

suspend fun IncomingCallResult.Companion.deserialize(
    json: JsonArray,
    resolveClassByUniqueId: (String) -> String
): Either<Ocpp16Error, IncomingCallResult> =
    either {
        val rawCall = parseRawCallResult(json).bind()
        val actionClassName = resolveClassByUniqueId(rawCall.uniqueId)
        parseAction(actionClassName, rawCall).bind()
    }

@Suppress("TooGenericExceptionCaught")
private fun parseRawCallResult(jsonArray: JsonArray): Either<Ocpp16Error, RawIncomingCallResult> {
    var uniqueId: String? = null
    return try {
        require(jsonArray.size() == CALL_RESULT_ENTRIES)

        val messageTypeId = jsonArray.get(MESSAGE_TYPE_ID_INDEX).asInt
        require(messageTypeId == CALL_MESSAGE_TYPE_ID)

        uniqueId = jsonArray.get(UNIQUE_ID_INDEX).asString

        val payload = jsonArray.get(PAYLOAD_INDEX).asJsonObject

        RawIncomingCallResult(messageTypeId, uniqueId, payload.toString()).right()
    } catch (_: Exception) {
        GenericError("Could not parse CallResult, invalid [json-]format", uniqueId).left()
    }
}

@Suppress("TooGenericExceptionCaught")
private fun parseAction(
    className: String,
    raw: RawIncomingCallResult
): Either<Ocpp16Error, IncomingCallResult> = try {
    val actionClass = IncomingMessageClassLoader.loadOcpp16IncomingResponseClass(className)

    // Use special jackson parser for action as gson doesn't call init block during class initialization
    val action = ocpp16JsonMapper.readValue(raw.payload, actionClass)

    IncomingCallResult(raw.uniqueId, action, raw.messageTypeId).right()
} catch (e: Exception) {
    val error = when (e) {
        is ValueInstantiationException -> {
            PropertyConstraintViolation()
        }
        else -> {
            FormationViolation(raw.uniqueId)
        }
    }
    error.left()
}

private data class RawIncomingCallResult(
    val messageTypeId: Int,
    val uniqueId: String,
    val payload: String
)


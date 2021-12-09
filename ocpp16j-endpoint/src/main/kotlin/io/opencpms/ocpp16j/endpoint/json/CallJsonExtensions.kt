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
import io.opencpms.ocpp16.protocol.IncomingMessageClassLoader
import io.opencpms.ocpp16.protocol.Ocpp16Error
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.protocol.FormationViolation
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.IncomingCall
import io.opencpms.ocpp16j.endpoint.protocol.OutgoingCall
import io.opencpms.ocpp16j.endpoint.protocol.PropertyConstraintViolation
import io.opencpms.ocpp16j.endpoint.util.GSON
import io.opencpms.ocpp16j.endpoint.util.JACKSON

private const val MESSAGE_TYPE_ID_INDEX = 0
private const val UNIQUE_ID_INDEX = 1
private const val ACTION_NAME_INDEX = 2
private const val PAYLOAD_INDEX = 3

fun OutgoingCall.serialize(): String {
    val payload = GSON.toJsonTree(payload)

    val jsonArray = JsonArray()
    jsonArray.add(messageTypeId)
    jsonArray.add(uniqueId)
    jsonArray.add(actionName)
    jsonArray.add(payload)

    return GSON.toJson(jsonArray)
}

suspend fun IncomingCall.Companion.deserialize(json: JsonArray): Either<Ocpp16Error, IncomingCall> = either {
    val rawCall = parseRawCall(json).bind()
    parseAction(rawCall).bind()
}

@Suppress("TooGenericExceptionCaught")
private fun parseRawCall(jsonArray: JsonArray): Either<Ocpp16Error, RawIncomingCall> {
    var uniqueId: String? = null
    return try {
        require(jsonArray.size() == CALL_ENTRIES)

        val messageTypeId = jsonArray.get(MESSAGE_TYPE_ID_INDEX).asInt
        require(messageTypeId == CALL_MESSAGE_TYPE_ID)

        uniqueId = jsonArray.get(UNIQUE_ID_INDEX).asString

        val actionName = jsonArray.get(ACTION_NAME_INDEX).asString

        val payload = jsonArray.get(PAYLOAD_INDEX).asJsonObject

        RawIncomingCall(messageTypeId, uniqueId, actionName, payload.toString()).right()
    } catch (_: Exception) {
        GenericError("Could not parse Call, invalid [json-]format", uniqueId).left()
    }
}

@Suppress("TooGenericExceptionCaught")
private fun parseAction(raw: RawIncomingCall): Either<Ocpp16Error, IncomingCall> = try {
    val actionName = raw.actionName
    val actionClass = IncomingMessageClassLoader.loadOcpp16IncomingRequestClass(actionName)

    // Use special jackson parser for action as gson doesn't call init block during class initialization
    val action = JACKSON.readValue(raw.payload, actionClass)

    IncomingCall(raw.uniqueId, actionName, action, raw.messageTypeId).right()
} catch (e: Exception) {
    val error = when (e) {
        is ValueInstantiationException -> {
            PropertyConstraintViolation(raw.uniqueId)
        }
        else -> {
            FormationViolation(raw.uniqueId)
        }
    }
    error.left()
}

private data class RawIncomingCall(
    val messageTypeId: Int,
    val uniqueId: String,
    val actionName: String,
    val payload: String
)

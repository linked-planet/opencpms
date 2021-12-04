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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.opencpms.ocpp16.service.Ocpp16Error
import io.opencpms.ocpp16j.endpoint.protocol.CALL_ERROR_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.protocol.CallError
import io.opencpms.ocpp16j.endpoint.protocol.GenericError
import io.opencpms.ocpp16j.endpoint.protocol.Ocpp16ErrorCode
import io.opencpms.ocpp16j.endpoint.util.GSON

private const val CALL_ERROR_ENTRIES = 5

private const val MESSAGE_TYPE_ID_INDEX = 0
private const val UNIQUE_ID_INDEX = 1
private const val ERROR_CODE_INDEX = 2
private const val ERROR_DESCRIPTION_INDEX = 3
private const val ERROR_DETAILS_INDEX = 4

@Suppress("TooGenericExceptionCaught")
object CallErrorTypeAdapter {

    fun serialize(callError: CallError): String {
        val payload = JsonObject()
        callError.errorDetails?.let { payload.addProperty("description", callError.errorDetails) }

        val jsonArray = JsonArray()
        jsonArray.add(callError.messageTypeId)
        jsonArray.add(callError.uniqueId)
        jsonArray.add(callError.errorCode.toString())
        jsonArray.add(callError.errorDescription)
        jsonArray.add(payload)
        return GSON.toJson(jsonArray)
    }

    suspend fun deserialize(json: String): Either<Ocpp16Error, CallError> = try {
        val jsonTree = JsonParser.parseString(json).asJsonArray
        parseCallError(jsonTree)
    } catch (_: Exception) {
        GenericError("Could not parse CallError, invalid [json-]format").left()
    }

    private fun parseCallError(jsonArray: JsonArray): Either<Ocpp16Error, CallError> {
        return try {
            require(jsonArray.size() == CALL_ERROR_ENTRIES)

            val messageTypeId = jsonArray.get(MESSAGE_TYPE_ID_INDEX).asInt
            require(messageTypeId == CALL_ERROR_MESSAGE_TYPE_ID)

            val uniqueId = jsonArray.get(UNIQUE_ID_INDEX).asString

            val errorCodeStr = jsonArray.get(ERROR_CODE_INDEX).asString
            val errorCode = Ocpp16ErrorCode.valueOf(errorCodeStr)

            val errorDescription = jsonArray.get(ERROR_DESCRIPTION_INDEX).asString

            val errorDetails = jsonArray.get(ERROR_DETAILS_INDEX).asJsonObject

            CallError(uniqueId, errorCode, errorDescription, errorDetails.toString()).right()
        } catch (_: Exception) {
            GenericError("Could not parse CallError, invalid [json-]format").left()
        }
    }
}

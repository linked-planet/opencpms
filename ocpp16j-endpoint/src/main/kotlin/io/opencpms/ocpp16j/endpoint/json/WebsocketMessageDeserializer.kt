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
import com.google.gson.*
import io.opencpms.ocpp16.protocol.*
import io.opencpms.ocpp16j.endpoint.protocol.*

object WebsocketMessageDeserializer {

    suspend fun deserialize(
        json: String,
        resolveClassByUniqueId: (String) -> String
    ): Either<Ocpp16Error, WebsocketMessage> = try {
        val messageJson = JsonParser.parseString(json).asJsonArray
        deserialize(messageJson, resolveClassByUniqueId)
    } catch (_: Exception) {
        GenericError("Invalid json").left()
    }

    private suspend fun deserialize(
        jsonArray: JsonArray,
        resolveClassByUniqueId: (String) -> String
    ): Either<Ocpp16Error, WebsocketMessage> =
        when (jsonArray.size()) {
            CALL_ENTRIES -> IncomingCall.deserialize(jsonArray)
            CALL_RESULT_ENTRIES -> IncomingCallResult.deserialize(jsonArray, resolveClassByUniqueId)
            CALL_ERROR_ENTRIES -> CallError.deserialize(jsonArray)
            else -> NotImplemented(details = "Unknown message format").left()
        }
}

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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.opencpms.ocpp16j.endpoint.protocol.CallError
import java.lang.reflect.Type

object CallErrorSerializer : JsonSerializer<CallError> {

    override fun serialize(src: CallError?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        require(src != null)

        val payload = JsonObject()
        src.errorDetails?.let { payload.addProperty("description", src.errorDetails) }

        val jsonArray = JsonArray()
        jsonArray.add(src.messageTypeId)
        jsonArray.add(src.uniqueId)
        jsonArray.add(src.errorCode.toString())
        jsonArray.add(src.errorDescription)
        jsonArray.add(payload)
        return jsonArray
    }
}

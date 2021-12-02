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
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.opencpms.ocpp16j.endpoint.protocol.CallResult
import io.opencpms.ocpp16j.endpoint.util.GSON
import java.lang.reflect.Type

object CallResultSerializer : JsonSerializer<CallResult> {

    override fun serialize(src: CallResult?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        require(src != null)

        val payload = GSON.toJsonTree(src.payload)

        val jsonArray = JsonArray()
        jsonArray.add(src.messageTypeId)
        jsonArray.add(src.uniqueId)
        jsonArray.add(payload)
        return jsonArray
    }
}

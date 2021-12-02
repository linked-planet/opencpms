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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import java.lang.reflect.Type

@Suppress("MagicNumber")
object RawCallDeserializer : JsonDeserializer<RawCall> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RawCall {
        require(json != null)

        val jsonArray = json.asJsonArray
        require(jsonArray.size() == 4)

        val messageTypeId = jsonArray.get(0).asInt
        require(messageTypeId == CALL_MESSAGE_TYPE_ID)

        val uniqueId = jsonArray.get(1).asString
        val actionName = jsonArray.get(2).asString
        val payload = jsonArray.get(3).asJsonObject

        return RawCall(uniqueId, actionName, messageTypeId, payload.toString())
    }
}

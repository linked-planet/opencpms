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
package io.opencpms.ocpp16j.endpoint.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull.INSTANCE
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.protocol.Call
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val GSON: Gson = GsonBuilder()
    .registerTypeAdapter(Call::class.java, CallDeserializer)
    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeTypeAdapter)
    .setPrettyPrinting()
    .create()

private const val OCPP16_PACKAGE_NAME = "io.opencpms.ocpp16.protocol.message"

@Suppress("MagicNumber")
object CallDeserializer : JsonDeserializer<Call> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Call {
        require(json != null)

        val jsonArray = json.asJsonArray
        require(jsonArray.size() == 4)

        val messageTypeId = jsonArray.get(0).asInt
        require(messageTypeId == CALL_MESSAGE_TYPE_ID) // = Call

        val uniqueId = jsonArray.get(1).asString
        val actionName = jsonArray.get(2).asString
        val payload = jsonArray.get(3).asJsonObject

        val actionClass = Class.forName("$OCPP16_PACKAGE_NAME.$actionName")
        val action = GSON.fromJson(payload, actionClass)

        return Call(uniqueId, actionName, action as Ocpp16IncomingMessage)
    }

}

object OffsetDateTimeTypeAdapter : JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OffsetDateTime {
        require(json != null)
        return OffsetDateTime.parse(json.asString)
    }

    override fun serialize(src: OffsetDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return src?.let { JsonPrimitive(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(src)) }
            ?: INSTANCE
    }
}

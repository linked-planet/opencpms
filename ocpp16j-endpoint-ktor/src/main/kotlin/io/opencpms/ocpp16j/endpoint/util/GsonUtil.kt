package io.opencpms.ocpp16j.endpoint.util

import com.google.gson.*
import com.google.gson.JsonNull.INSTANCE
import io.opencpms.ocpp16.protocol.Ocpp16IncomingMessage
import io.opencpms.ocpp16j.endpoint.protocol.Call
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

val GSON: Gson = GsonBuilder()
    .registerTypeAdapter(Call::class.java, CallDeserializer)
    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeTypeAdapter)
    .setPrettyPrinting()
    .create()

const val OCPP16_PACKAGE_NAME = "io.opencpms.ocpp16.protocol.message"

object CallDeserializer : JsonDeserializer<Call> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Call {
        require(json != null)

        val jsonArray = json.asJsonArray
        require(jsonArray.size() == 4)

        val messageTypeId = jsonArray.get(0).asInt
        require(messageTypeId == 2) // = Call

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
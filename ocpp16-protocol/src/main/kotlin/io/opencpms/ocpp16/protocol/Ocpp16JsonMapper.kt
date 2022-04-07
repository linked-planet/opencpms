package io.opencpms.ocpp16.protocol

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opencpms.ocpp16.protocol.message.*

val ocpp16JsonMapper = jacksonObjectMapper().apply {
    registerSubtypes(NamedType(BootNotificationRequest::class.java, "BootNotification"))
    registerSubtypes(NamedType(CancelReservationResponse::class.java, "CancelReservation"))
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // to get ISO timestamps instead
}

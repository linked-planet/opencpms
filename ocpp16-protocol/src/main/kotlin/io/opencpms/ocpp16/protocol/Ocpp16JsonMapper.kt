package io.opencpms.ocpp16.protocol

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.opencpms.ocpp16.protocol.message.BootNotificationRequest

val ocpp16JsonMapper = jacksonObjectMapper().apply {
    registerSubtypes(NamedType(BootNotificationRequest::class.java, "BootNotification"))
}

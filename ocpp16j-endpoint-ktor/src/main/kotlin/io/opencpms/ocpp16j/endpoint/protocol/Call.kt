package io.opencpms.ocpp16j.endpoint.protocol

import io.opencpms.ocpp16.protocol.*

data class Call(
    val uniqueId: String,
    val actionName: String,
    val payload: Ocpp16IncomingMessage
) {
    val messageTypeId = 2
}

interface CallResponse

data class CallResult(
    val uniqueId: String,
    val payload: Ocpp16OutgoingMessage
) : CallResponse {
    val messageTypeId = 3
}

data class CallError(
    val uniqueId: String,
    val errorCode: Ocpp16ErrorCode,
    val errorDescription: String = "",
    val errorDetails: String
) : CallResponse {
    val messageTypeId: Int = 4
}

enum class Ocpp16ErrorCode {
    NotImplemented,
    NotSupported,
    InternalError,
    ProtocolError,
    SecurityError,
    FormationViolation,
    PropertyConstraintViolation,
    OccurenceConstraintViolation,
    TypeConstraintViolation,
    GenericError
}
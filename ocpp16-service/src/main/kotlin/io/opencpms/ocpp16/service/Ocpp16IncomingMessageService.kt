package io.opencpms.ocpp16.service

import arrow.core.Either
import io.opencpms.ocpp16.protocol.*

interface Ocpp16IncomingMessageService {

    fun handleMessage(session: Ocpp16Session, message: Ocpp16IncomingMessage): Either<Error, Ocpp16OutgoingMessage>
}
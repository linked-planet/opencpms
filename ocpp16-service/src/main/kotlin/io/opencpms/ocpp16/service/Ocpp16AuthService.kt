package io.opencpms.ocpp16.service

import arrow.core.Either

interface Ocpp16AuthService {

    fun authenticateChargePoint(chargePointId: String): Either<Error, Unit>

    fun authenticateChargePointWithAuthKey(chargePointId: String, authKey: String): Either<Error, Unit>
}

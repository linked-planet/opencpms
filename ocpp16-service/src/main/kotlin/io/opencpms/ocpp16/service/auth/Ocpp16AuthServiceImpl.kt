package io.opencpms.ocpp16.service.auth

import arrow.core.Either
import arrow.core.right

class Ocpp16AuthServiceImpl : Ocpp16AuthService {

    override fun authenticateChargePoint(chargePointId: String): Either<Error, Unit> {
        return Unit.right()
    }

    override fun authenticateChargePointWithAuthKey(chargePointId: String, authKey: String): Either<Error, Unit> {
        return Unit.right()
    }
}

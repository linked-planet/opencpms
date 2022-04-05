/**
 * OpenCPMS
 * Copyright (C) 2022 linked-planet GmbH (info@linked-planet.com).
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
package io.opencpms.ocpp16.service.receiver

import arrow.core.Either
import arrow.core.right
import io.opencpms.ocpp16.protocol.Ocpp16Error
import io.opencpms.ocpp16.protocol.Ocpp16IncomingRequest
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingResponse
import io.opencpms.ocpp16.protocol.message.BootNotificationResponse
import io.opencpms.ocpp16.service.session.Ocpp16Session
import java.time.OffsetDateTime

@Suppress("MagicNumber")
class Ocpp16MessageReceiverImpl : Ocpp16MessageReceiver {

    override fun handleMessage(
        session: Ocpp16Session,
        message: Ocpp16IncomingRequest
    ): Either<Ocpp16Error, Ocpp16OutgoingResponse> {
        return BootNotificationResponse(BootNotificationResponse.Status.Accepted, OffsetDateTime.now(), 10L).right()
    }
}

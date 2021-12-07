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
package io.opencpms.ocpp16.service.sender

import arrow.core.Either
import io.opencpms.ocpp16.protocol.Ocpp16Error
import io.opencpms.ocpp16.protocol.Ocpp16IncomingResponse
import io.opencpms.ocpp16.protocol.Ocpp16OutgoingRequest
import io.opencpms.ocpp16.service.session.Ocpp16Session

interface Ocpp16MessageSender {

    fun sendMessage(
        session: Ocpp16Session,
        message: Ocpp16OutgoingRequest
    ): Either<Ocpp16Error, Ocpp16IncomingResponse>
}

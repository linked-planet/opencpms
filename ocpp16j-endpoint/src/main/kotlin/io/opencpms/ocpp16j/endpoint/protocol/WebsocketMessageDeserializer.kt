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
package io.opencpms.ocpp16j.endpoint.protocol

import arrow.core.Either
import arrow.core.Either.Companion.catch
import io.opencpms.ocpp16.protocol.*

object WebsocketMessageDeserializer {

    private const val CALL_ENTRIES = 4
    private const val CALL_RESULT_ENTRIES = 3
    private const val CALL_ERROR_ENTRIES = 5

    @Suppress("TooGenericExceptionThrown")
    fun deserialize(json: String): Either<Ocpp16Error, WebsocketMessage> =
        catch {
            // TODO instead derive from message type id:
            //  CALL: 2
            //  CALLRESULT: 3
            //  CALLERROR: 4
            when (ocpp16JsonMapper.readTree(json).size()) {
                CALL_ENTRIES -> ocpp16JsonMapper.readValue(json, IncomingCall::class.java)
                CALL_RESULT_ENTRIES -> ocpp16JsonMapper.readValue(json, IncomingCallResult::class.java)
                CALL_ERROR_ENTRIES -> ocpp16JsonMapper.readValue(json, CallError::class.java)
                else -> throw RuntimeException("Unknown message format: $json")
            }
        }
            .mapLeft { GenericError("Invalid json: $it") }

}

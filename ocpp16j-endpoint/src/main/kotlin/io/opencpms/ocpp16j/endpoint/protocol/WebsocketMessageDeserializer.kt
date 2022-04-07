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

    @Suppress("TooGenericExceptionThrown", "MagicNumber")
    fun deserialize(json: String): Either<Ocpp16Error, WebsocketMessage> =
        catch {
            // TODO can be programmed such that we extract more data from the base array
            //      to provide this information in case there are errors
            val node = ocpp16JsonMapper.readTree(json)
            when (node[0].asInt()) {
                2 -> ocpp16JsonMapper.readValue(json, IncomingCall::class.java)
                3 -> ocpp16JsonMapper.readValue(json, IncomingCallResult::class.java)
                4 -> ocpp16JsonMapper.readValue(json, CallError::class.java)
                else -> throw RuntimeException("Unknown message format: $json")
            }
        }
            .mapLeft { FormationViolation() }

}

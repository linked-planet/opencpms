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
package io.opencpms.ocpp16.service

const val UNKNOWN_UNIQUE_ID = "UNKNOWN"

open class Ocpp16Error(uniqueId: String?, val reason: String, val details: String? = null) {
    val uniqueId: String = uniqueId ?: UNKNOWN_UNIQUE_ID
}

class NotSupportedError(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String?) : Ocpp16Error(
    uniqueId,
    "Requested Action is recognized but not supported by the receiver",
    details,
)

class InternalError(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "An internal error occurred and the receiver was not able to process the requested Action successfully",
    details
)

class ProtocolError(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "Payload for Action is incomplete",
    details
)

class SecurityError(uniqueId: String? = UNKNOWN_UNIQUE_ID, details: String? = null) : Ocpp16Error(
    uniqueId,
    "During the processing of Action a security issue occurred " +
            "preventing receiver from completing the Action successfully",
    details
)

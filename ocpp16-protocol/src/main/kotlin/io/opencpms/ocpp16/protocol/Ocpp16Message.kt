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
package io.opencpms.ocpp16.protocol

private const val OCPP16_REQUEST_SUFFIX = "Request"

private const val OCPP16_RESPONSE_SUFFIX = "Response"

interface Ocpp16Request {
    fun getActionName() = this.javaClass.name.removeSuffix(OCPP16_REQUEST_SUFFIX)
}

interface Ocpp16Response {
    fun getActionName(): String = this.javaClass.name.removeSuffix(OCPP16_RESPONSE_SUFFIX)
}

/**
 * Marks an OCPP1.6 request which is sent from CS -> CP (= outgoing).
 */
interface Ocpp16OutgoingRequest : Ocpp16Request

/**
 * Marks an OCPP1.6 response which is sent from CS -> CP (= outgoing).
 */
interface Ocpp16OutgoingResponse : Ocpp16Response

/**
 * Marks an OCPP1.6 request which is sent from CP -> CS (= incoming).
 */
interface Ocpp16IncomingRequest : Ocpp16Request

/**
 * Marks an OCPP1.6 response which is sent from CP -> CS (= incoming).
 */
interface Ocpp16IncomingResponse : Ocpp16Response

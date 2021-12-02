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

open class Ocpp16Error(val reason: String, val details: String? = null)

class NotSupportedError(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class InternalError(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class ProtocolError(reason: String, details: String? = null) : Ocpp16Error(reason, details)
class SecurityError(reason: String, details: String? = null) : Ocpp16Error(reason, details)

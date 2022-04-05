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
package io.opencpms.ocpp16.protocol.message

import io.opencpms.ocpp16.protocol.Ocpp16IncomingRequest

data class BootNotificationRequest(
    val chargePointVendor: String,
    val chargePointModel: String,
    val chargePointSerialNumber: String? = null,
    val chargeBoxSerialNumber: String? = null,
    val firmwareVersion: String? = null,
    val iccid: String? = null,
    val imsi: String? = null,
    val meterType: String? = null,
    val meterSerialNumber: String? = null
) : Ocpp16IncomingRequest {

    init {
        require(chargePointVendor.length <= 20) { "chargePointVendor length > maximum 20 - ${chargePointVendor.length}" }
        require(chargePointModel.length <= 20) { "chargePointModel length > maximum 20 - ${chargePointModel.length}" }
        if (chargePointSerialNumber != null)
            require(chargePointSerialNumber.length <= 25) { "chargePointSerialNumber length > maximum 25 - ${chargePointSerialNumber.length}" }
        if (chargeBoxSerialNumber != null)
            require(chargeBoxSerialNumber.length <= 25) { "chargeBoxSerialNumber length > maximum 25 - ${chargeBoxSerialNumber.length}" }
        if (firmwareVersion != null)
            require(firmwareVersion.length <= 50) { "firmwareVersion length > maximum 50 - ${firmwareVersion.length}" }
        if (iccid != null)
            require(iccid.length <= 20) { "iccid length > maximum 20 - ${iccid.length}" }
        if (imsi != null)
            require(imsi.length <= 20) { "imsi length > maximum 20 - ${imsi.length}" }
        if (meterType != null)
            require(meterType.length <= 25) { "meterType length > maximum 25 - ${meterType.length}" }
        if (meterSerialNumber != null)
            require(meterSerialNumber.length <= 25) { "meterSerialNumber length > maximum 25 - ${meterSerialNumber.length}" }
    }

}
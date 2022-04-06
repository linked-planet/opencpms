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
package io.opencpms.ocpp16j.endpoint.integrationtest

import chargepoint.docile.test.`TestPassed$`
import org.junit.Test
import kotlin.test.*

class BootNotificationIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testBootNotification() {
        val status = runChargePointIntegrationTest("BootNotification", "bootNotification()")
        assertNotNull(status)
        assertTrue(status is `TestPassed$`, "Test did not pass")
    }

    @Test
    fun testBootNotification2() {
        val status = runChargePointIntegrationTest("BootNotification2", "bootNotification()")
        assertNotNull(status)
        assertTrue(status is `TestPassed$`, "Test did not pass")
    }

}

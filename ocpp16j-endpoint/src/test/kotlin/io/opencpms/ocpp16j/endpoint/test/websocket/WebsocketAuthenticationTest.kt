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
package io.opencpms.ocpp16j.endpoint.test.websocket

import arrow.core.*
import io.mockk.*
import io.opencpms.ocpp16j.endpoint.auth.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.test.util.*
import org.junit.*
import kotlin.test.assertEquals

class WebsocketAuthenticationTest {

    private val authService = mockk<Ocpp16AuthService>()

    @After
    fun resetAppConfig() {
        clearAllMocks()
    }

    @Test
    fun testAcceptsKnownChargePointId() {
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        withTestApplication(basicAuthEnabled = false, authService) {
            val response = handleWebSocket("/ocpp/16/known",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                }).response
            assertEquals("101 Switching Protocols", response.status().toString())
        }

        verify(exactly = 1) { authService.authenticateChargePoint("known") }
    }

    @Test
    fun testRefusesUnknownChargePointId() {
        every { authService.authenticateChargePoint(any()) }.returns(Error().left())

        withTestApplication(basicAuthEnabled = false, authService) {
            val response = handleWebSocket("/ocpp/16/unknown",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                }).response
            assertEquals("404 Not Found", response.status().toString())
        }

        verify(exactly = 1) { authService.authenticateChargePoint("unknown") }
    }

    @Test
    fun testAcceptsCorrectBasicAuth() {
        every { authService.authenticateChargePointWithAuthKey(any(), any()) }.returns(Unit.right())

        withTestApplication(basicAuthEnabled = true, authService) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")

                    val encodedBasicAuthHeader = encodeBase64("test:correct")
                    this.addHeader("Authorization", "Basic $encodedBasicAuthHeader")
                }).response
            assertEquals("101 Switching Protocols", response.status().toString())
        }

        verify(exactly = 1) { authService.authenticateChargePointWithAuthKey("test", "correct") }
    }

    @Test
    fun testRefusesIncorrectBasicAuth() {
        every { authService.authenticateChargePointWithAuthKey(any(), any()) }.returns(Error().left())

        withTestApplication(basicAuthEnabled = true, authService) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")

                    val encodedBasicAuthHeader = encodeBase64("test:incorrect")
                    this.addHeader("Authorization", "Basic $encodedBasicAuthHeader")
                }).response
            assertEquals("404 Not Found", response.status().toString())
        }

        verify(exactly = 1) { authService.authenticateChargePointWithAuthKey("test", "incorrect") }
    }
}

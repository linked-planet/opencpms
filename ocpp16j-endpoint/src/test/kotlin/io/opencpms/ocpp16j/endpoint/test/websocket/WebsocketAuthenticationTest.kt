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
package io.opencpms.ocpp16j.endpoint.test.websocket

import arrow.core.left
import arrow.core.right
import io.ktor.server.testing.withTestApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.opencpms.ocpp16.service.auth.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.junit.After
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import io.opencpms.ocpp16j.endpoint.test.util.encodeBase64
import kotlin.test.assertEquals

class WebsocketAuthenticationTest {

    private val appConfig = spyk<AppConfig>()
    private val authService = mockk<Ocpp16AuthService>()

    private val testContext = DI {
        bind { singleton { appConfig } }
        bind { singleton { authService } }
    }

    @After
    fun resetAppConfig() {
        clearAllMocks()
    }

    @Test
    fun testAcceptsKnownChargePointId() {
        every { appConfig.useBasicAuth } returns false
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        withTestApplication({ configureSockets(testContext) }) {
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
        every { appConfig.useBasicAuth } returns false
        every { authService.authenticateChargePoint(any()) }.returns(Error().left())

        withTestApplication({ configureSockets(testContext) }) {
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
        every { appConfig.useBasicAuth } returns true
        every { authService.authenticateChargePointWithAuthKey(any(), any()) }.returns(Unit.right())

        withTestApplication({ configureSockets(testContext) }) {
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
        every { appConfig.useBasicAuth } returns true
        every { authService.authenticateChargePointWithAuthKey(any(), any()) }.returns(Error().left())

        withTestApplication({ configureSockets(testContext) }) {
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

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

import arrow.core.right
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.opencpms.ocpp16.service.auth.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.junit.After
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.test.assertEquals

class WebsocketEndpointTest {

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
    fun testWebsocketAcceptsOcpp16() {
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        withTestApplication({ configureSockets(testContext) }) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                }).response
            assertEquals("101 Switching Protocols", response.status().toString())
        }
    }

    @Test
    fun testWebsocketAcceptsOcpp16OutOfMultipleProtocols() {
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        withTestApplication({ configureSockets(testContext) }) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6,ocpp1.5")
                }).response
            assertEquals("101 Switching Protocols", response.status().toString())
        }
    }


    /**
     * Deviation to OCPP1.6 - should be:
     * If the Central System does not agree to using one of the subprotocols offered by the client, it MUST complete the
     * WebSocket handshake with a response without a Sec-WebSocket-Protocol header and then immediately close the
     * WebSocket connection.
     *
     * BUT: this is default behaviour of ktor
     */
    @Test
    fun testWebsocketDeniesOtherProtocolsThanOcpp16() {
        withTestApplication({ configureSockets(testContext) }) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "karl")
                }).response
            assertEquals("404 Not Found", response.status().toString())
        }
    }

    /**
     * Deviation to OCPP1.6 - should be:
     * If the Central System does not agree to using one of the subprotocols offered by the client, it MUST complete the
     * WebSocket handshake with a response without a Sec-WebSocket-Protocol header and then immediately close the
     * WebSocket connection.
     *
     * BUT: this is default behaviour of ktor
     */
    @Test
    fun testWebsocketDeniesNoProtocol() {
        withTestApplication({ configureSockets(testContext) }) {
            val response = handleWebSocket("/ocpp/16/test", setup = {}).response
            assertEquals("404 Not Found", response.status().toString())
        }
    }

    @Test
    fun testRespondsNotFoundForPlainHttpRequest() {
        withTestApplication({ configureSockets(testContext) }) {
            handleRequest(HttpMethod.Get, "/ocpp/16/test").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}

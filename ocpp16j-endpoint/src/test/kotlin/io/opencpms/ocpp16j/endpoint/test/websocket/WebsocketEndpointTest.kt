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
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import io.opencpms.ocpp16j.endpoint.auth.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.test.util.withTestApplication
import org.junit.*
import kotlin.test.assertEquals

class WebsocketEndpointTest {

    private val authService = mockk<Ocpp16AuthService>()

    @After
    fun resetAppConfig() {
        clearAllMocks()
    }

    @Test
    fun testWebsocketAcceptsOcpp16() {
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        withTestApplication(basicAuthEnabled = false, authService) {
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

        withTestApplication(basicAuthEnabled = false, authService) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6,ocpp1.5")
                }).response
            assertEquals("101 Switching Protocols", response.status().toString())
        }
    }


    /**
     * Deviation to OCPP1.6 - should be:
     * If the Central System does not agree to using one of the sub protocols offered by the client, it MUST complete
     * the WebSocket handshake with a response without a Sec-WebSocket-Protocol header and then immediately close the
     * WebSocket connection.
     *
     * BUT: this is default behaviour of ktor
     */
    @Test
    fun testWebsocketDeniesOtherProtocolsThanOcpp16() {
        withTestApplication(basicAuthEnabled = false, authService) {
            val response = handleWebSocket("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "karl")
                }).response
            assertEquals("404 Not Found", response.status().toString())
        }
    }

    /**
     * Deviation to OCPP1.6 - should be:
     * If the Central System does not agree to using one of the sub protocols offered by the client, it MUST complete
     * the WebSocket handshake with a response without a Sec-WebSocket-Protocol header and then immediately close the
     * WebSocket connection.
     *
     * BUT: this is default behaviour of ktor
     */
    @Test
    fun testWebsocketDeniesNoProtocol() {
        withTestApplication(basicAuthEnabled = false, authService) {
            val response = handleWebSocket("/ocpp/16/test", setup = {}).response
            assertEquals("404 Not Found", response.status().toString())
        }
    }

    @Test
    fun testRespondsNotFoundForPlainHttpRequest() {
        withTestApplication(basicAuthEnabled = false, authService) {
            handleRequest(HttpMethod.Get, "/ocpp/16/test").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}

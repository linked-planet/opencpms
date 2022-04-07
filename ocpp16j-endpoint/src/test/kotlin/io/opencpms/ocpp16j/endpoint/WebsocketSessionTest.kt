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
package io.opencpms.ocpp16j.endpoint

import arrow.core.right
import io.ktor.http.cio.websocket.*
import io.mockk.*
import io.opencpms.ocpp16j.endpoint.auth.Ocpp16AuthService
import org.junit.*
import org.junit.Test
import org.skyscreamer.jsonassert.*
import kotlin.test.*

class WebsocketSessionTest : AbstractWebsocketTest() {

    private val authService = mockk<Ocpp16AuthService>()

    @After
    fun resetMocks() {
        clearAllMocks()
    }

    @Test
    fun testCallWithoutMessageTypeId() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {
                            "chargePointVendor": "ex dolor",
                            "chargePointModel": "veniam voluptate u"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun testCallWithoutUniqueId() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "BootNotification",
                          {
                            "chargePointVendor": "ex dolor",
                            "chargePointModel": "veniam voluptate u"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun testCallWithoutAction() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          {
                            "chargePointVendor": "ex dolor",
                            "chargePointModel": "veniam voluptate u"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun testCallWithoutPayload() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification"
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun testCallWithInvalidJsonPayload() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {
                            "chargePointVendor": "ex dolor",
                            "chargePointModel": "veniam voluptate u"

                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun `test call with invalid empty action`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {}
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun `test call with invalid action which is missing a required attribute`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {
                            "chargePointVendor": "ex dolor"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun `test call with invalid action whose attribute value is invalid`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {
                            "chargePointVendor": "ex dolor"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

    @Test
    fun `test call with invalid action whose attribute value is out range`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = """
                         [
                          2,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "BootNotification",
                          {
                            "chargePointVendor": "tooooooooooooooooooooooo looooooooooooooooooooooooooooooooooong",
                            "chargePointModel": "veniam voluptate u"
                          }
                        ]
                    """.trimIndent()
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "UNKNOWN",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          null
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponseJson, response, JSONCompareMode.LENIENT)
                }
            )
        }
    }

}

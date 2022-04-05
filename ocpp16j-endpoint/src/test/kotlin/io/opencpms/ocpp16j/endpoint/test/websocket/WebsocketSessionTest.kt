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
import io.ktor.http.cio.websocket.*
import io.mockk.*
import io.opencpms.ocpp16.protocol.ProtocolError
import io.opencpms.ocpp16.protocol.message.BootNotificationResponse
import io.opencpms.ocpp16.service.auth.Ocpp16AuthService
import io.opencpms.ocpp16.service.receiver.Ocpp16MessageReceiver
import io.opencpms.ocpp16j.endpoint.protocol.CALL_MESSAGE_TYPE_ID
import io.opencpms.ocpp16j.endpoint.test.util.*
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.test.*

class WebsocketSessionTest {

    private val authService = mockk<Ocpp16AuthService>()
    private val incomingMessageService = mockk<Ocpp16MessageReceiver>()

    @Test
    fun testCallAndCallResult() {
        // Mocking
        val mockResponse = BootNotificationResponse(
            BootNotificationResponse.Status.Accepted,
            OffsetDateTime.parse("2021-12-02T20:45:12.208+01:00"),
            10L
        )
        val mockUniqueId = "adb4f199-89fa-40b0-86c0-29b5ec1bd253"
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())
        every { incomingMessageService.handleMessage(any(), any()) }.returns(mockResponse.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = createTestCall(mockUniqueId)
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          3,
                          "adb4f199-89fa-40b0-86c0-29b5ec1bd253",
                          {
                            "status": "Accepted",
                            "currentTime": "2021-12-02T20:45:12.208+01:00",
                            "interval": 10
                          }
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }

        verify(exactly = 1) { incomingMessageService.handleMessage(any(), any()) }
    }

    @Test
    fun testCallAndCallError() {
        // Mocking
        val mockUUID = "d16d2312-03fe-4dd8-8d06-ea29b7ca2269"
        val mockResponse = ProtocolError(
            mockUUID,
            "details"
        )
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())
        every { incomingMessageService.handleMessage(any(), any()) }.returns(mockResponse.left())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                },
                callback = { incoming, outgoing ->
                    val call = createTestCall(mockUUID)
                    outgoing.send(Frame.Text(call))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)

                    val expectedResponseJson = """
                        [
                          4,
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "ProtocolError",
                          "Payload for Action is incomplete",
                          {
                            "description": "details"
                          }
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }

        verify(exactly = 1) { incomingMessageService.handleMessage(any(), any()) }
    }

    @Test
    fun testCallWithoutMessageTypeId() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "GenericError",
                          "Could not parse CallResult, invalid [json-]format",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    fun testCallWithoutUniqueId() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "BootNotification",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    fun testCallWithoutAction() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    fun testCallWithoutPayload() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "GenericError",
                          "Could not parse CallResult, invalid [json-]format",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    fun testCallWithInvalidJsonPayload() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "GenericError",
                          "Invalid json",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `test call with invalid empty action`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `test call with invalid action which is missing a required attribute`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `test call with invalid action whose attribute value is invalid`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "FormationViolation",
                          "Payload for Action is syntactically incorrect or not conform to the PDU structure for Action",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    @Test
    fun `test call with invalid action whose attribute value is out range`() {
        // Mocking
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())

        // Test
        withTestApplication(basicAuthEnabled = false, authService, ocpp16MessageReceiver = incomingMessageService) {
            handleWebSocketConversation("/ocpp/16/test",
                setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
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
                          "d16d2312-03fe-4dd8-8d06-ea29b7ca2269",
                          "PropertyConstraintViolation",
                          "Payload is syntactically correct but at least one field contains an invalid value",
                          {}
                        ]
                    """.trimIndent()
                    assertEquals(expectedResponseJson, response)
                }
            )
        }
    }

    private fun createTestCall(uniqueId: String): String {
        val actionPath = "src/test/resources/BootNotificationRequest.json"
        val actionName = "BootNotification"
        val actionJsonStr = readFileAsText(actionPath)
        return "[$CALL_MESSAGE_TYPE_ID,\"$uniqueId\",\"$actionName\", $actionJsonStr]"
    }
}

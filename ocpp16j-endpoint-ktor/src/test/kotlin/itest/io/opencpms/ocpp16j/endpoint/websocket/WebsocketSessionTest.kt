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
package itest.io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.right
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.opencpms.ocpp16.protocol.message.BootNotificationResponse
import io.opencpms.ocpp16.service.Ocpp16AuthService
import io.opencpms.ocpp16.service.Ocpp16IncomingMessageService
import io.opencpms.ocpp16.service.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import java.io.File
import java.time.OffsetDateTime
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.test.assertNotNull

class WebsocketSessionTest {

    private val appConfig = spyk<AppConfig>()
    private val authService = mockk<Ocpp16AuthService>()
    private val incomingMessageService = mockk<Ocpp16IncomingMessageService>()

    private val testContext = DI {
        bind { singleton { appConfig } }
        bind { singleton { authService } }
        bind { singleton { incomingMessageService } }

        bind { singleton { Ocpp16SessionManager() } }
    }

    @Test
    fun testMessage() {
        every { authService.authenticateChargePoint(any()) }.returns(Unit.right())
        every { incomingMessageService.handleMessage(any(), any()) }.returns(
            BootNotificationResponse(
                BootNotificationResponse.Status.Accepted,
                OffsetDateTime.now(),
                0
            ).right()
        )

        withTestApplication({ configureSockets(testContext) }) {
            handleWebSocketConversation("/ocpp/16/test",
                callback = { incoming, outgoing ->
                    val file = File("src/test/resources/BootNotificationRequest.json")
                    val actionName = file.name.substringBeforeLast(".").substringBeforeLast("-")
                    val requestActionJsonStr = file.readText()
                    outgoing.send(Frame.Text("[2,\"19223201\",\"$actionName\", $requestActionJsonStr]"))

                    val response = (incoming.receive() as Frame.Text).readText()
                    assertNotNull(response)
                }, setup = {
                    this.addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                }
            )
        }
    }
}

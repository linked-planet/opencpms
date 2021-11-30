package itest.io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.right
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import io.mockk.*
import io.opencpms.ocpp16.protocol.message.BootNotificationResponse
import io.opencpms.ocpp16.service.*
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.junit.Test
import org.kodein.di.*
import java.io.File
import java.time.OffsetDateTime
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
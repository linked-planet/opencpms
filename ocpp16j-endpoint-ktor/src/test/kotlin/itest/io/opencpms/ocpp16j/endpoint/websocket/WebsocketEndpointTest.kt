package itest.io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.right
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import io.opencpms.ocpp16.service.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.junit.*
import org.kodein.di.*
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
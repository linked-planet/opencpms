package itest.io.opencpms.ocpp16j.endpoint.websocket

import arrow.core.*
import io.ktor.server.testing.*
import io.mockk.*
import io.opencpms.ocpp16.service.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.junit.*
import org.kodein.di.*
import util.encodeBase64
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
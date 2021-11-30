package itest.io.opencpms.ocpp16j.endpoint

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.mockk.*
import io.opencpms.ocpp16j.endpoint.*
import io.opencpms.ocpp16j.endpoint.config.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.io.File
import javax.net.ssl.SSLHandshakeException
import kotlin.concurrent.thread
import kotlin.test.assertFailsWith

class ApplicationTlsTest {
    private lateinit var serverThread: Thread

    @Before
    fun setup() {
        serverThread = thread {
            val appConfig = mockk<AppConfig>()
            every { appConfig.hostname }.returns("127.0.0.1")
            every { appConfig.port }.returns(9090)
            every { appConfig.useBasicAuth }.returns(false)
            every { appConfig.useTls }.returns(true)
            every { appConfig.tlsConfig }.returns(
                TlsConfig(
                    File("src/test/resources/keystore.jks"),
                    "bar",
                    "sample",
                    "foo"
                )
            )

            val context = createContext(appConfig)
            val environment = createApplicationEngineEnvironment(appConfig, context)

            embeddedServer(Jetty, environment).start(wait = true)
        }
    }

    @After
    fun teardown() {
        serverThread.stop()
    }

    @Test
    fun testTls() {
        Thread.sleep(5000)
        assertFailsWith<SSLHandshakeException> {
            runBlocking {
                HttpClient()
                    .webSocket("https://127.0.0.1:9090/ocpp/16/test") {}
            }
        }
    }
}
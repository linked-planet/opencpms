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
package itest.io.opencpms.ocpp16j.endpoint

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.webSocket
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.mockk.every
import io.mockk.mockk
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.config.TlsConfig
import io.opencpms.ocpp16j.endpoint.createApplicationEngineEnvironment
import io.opencpms.ocpp16j.endpoint.createContext
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
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

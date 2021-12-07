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
package io.opencpms.ocpp16j.endpoint.integrationtest.util

import chargepoint.docile.dsl.AwaitTimeoutInMillis
import chargepoint.docile.test.Loader
import chargepoint.docile.test.Repeat
import chargepoint.docile.test.RunnerConfig
import chargepoint.docile.test.TestResult
import com.thenewmotion.ocpp.Version
import com.thenewmotion.ocpp.VersionFamily
import io.ktor.client.utils.EmptyContent.status
import io.ktor.server.jetty.JettyApplicationEngine
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.createContext
import io.opencpms.ocpp16j.endpoint.startServer
import java.net.URI
import org.junit.After
import org.junit.Before
import org.kodein.di.DI
import scala.Option
import scala.collection.JavaConversions
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.Seq
import scala.collection.immutable.Map
import javax.net.ssl.SSLContext

abstract class BaseIntegrationTest {

    lateinit var server: JettyApplicationEngine

    @Before
    fun setup() {
        val appConfig = createCentralServiceConfig()
        val context = createCentralServiceContext(appConfig)
        server = startServer(appConfig, context, wait = false)
    }

    @After
    fun teardown() {
        server.stop(1000, 10000)
    }

    fun runChargePointIntegrationTest(testName: String, testScript: String): TestResult? {
        val runner = Loader.runnerFor(VersionFamily.`V1X$`(), testName, testScript.toByteArray())
        val config = createChargePointRunnerConfig()
        val scalaStatus = runner.run(config)
        return mapAsJavaMap(seqAsJavaList(mapAsJavaMap(scalaStatus)[config.chargePointId()])[0])[testName]
    }

    open fun createChargePointRunnerConfig(): RunnerConfig = RunnerConfig(
        1,
        "123456",
        URI("http://localhost:9090/ocpp/16"),
        Version.`V16$`(),
        Option.empty(),
        SSLContext.getDefault(),
        Repeat(1, 0),
        AwaitTimeoutInMillis(5000)
    )

    open fun createCentralServiceConfig(): AppConfig = AppConfig()

    open fun createCentralServiceContext(appConfig: AppConfig): DI = createContext(appConfig)
}

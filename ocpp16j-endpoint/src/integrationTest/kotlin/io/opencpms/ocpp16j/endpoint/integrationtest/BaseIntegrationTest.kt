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
package io.opencpms.ocpp16j.endpoint.integrationtest

import chargepoint.docile.dsl.AwaitTimeoutInMillis
import chargepoint.docile.test.*
import com.thenewmotion.ocpp.*
import scala.Option
import scala.collection.JavaConverters.*
import java.net.URI
import javax.net.ssl.SSLContext

abstract class BaseIntegrationTest {

    fun runChargePointIntegrationTest(testName: String, testScript: String): TestResult? {
        val runner = Loader.runnerFor(VersionFamily.`V1X$`(), testName, testScript.toByteArray())
        val config = createChargePointRunnerConfig()
        val scalaStatus = runner.runOneCase(config)
        return mapAsJavaMap(seqAsJavaList(scalaStatus)[0])[testName]
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

}

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
package io.opencpms.ocpp16j.endpoint

import chargepoint.docile.dsl.AwaitTimeoutInMillis
import chargepoint.docile.test.Loader
import chargepoint.docile.test.Repeat
import chargepoint.docile.test.RunnerConfig
import com.thenewmotion.ocpp.VersionFamily
import java.net.URI
import org.junit.Test
import scala.Option
import javax.net.ssl.SSLContext

class IntegrationTest {

    @Test
    fun test() {
        val runner = Loader.runnerFor(VersionFamily.`V1X$`(), "test", "bootNotification()".toByteArray())
        val config = RunnerConfig(
            1,
            "123456",
            URI("http://localhost:9090/ocpp/16"),
            com.thenewmotion.ocpp.Version.`V16$`(),
            Option.empty(),
            SSLContext.getDefault(),
            Repeat(1, 0),
            AwaitTimeoutInMillis(500000)
        )

        val status = runner.run(config)
        System.out.println(status)
    }
}
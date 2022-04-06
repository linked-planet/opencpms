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
package io.opencpms.ocpp16j.endpoint.test.util

import io.ktor.config.*
import io.ktor.server.testing.*
import io.opencpms.ocpp16j.endpoint.auth.*
import io.opencpms.ocpp16j.endpoint.main
import io.opencpms.ocpp16j.endpoint.session.Ocpp16SessionManager
import org.kodein.di.*

fun withTestApplication(
    basicAuthEnabled: Boolean,
    ocpp16AuthService: Ocpp16AuthService = Ocpp16AuthServiceImpl(),
    ocpp16SessionManager: Ocpp16SessionManager = Ocpp16SessionManager(),
    test: TestApplicationEngine.() -> Unit
) {
    withTestApplication {
        (environment.config as MapApplicationConfig).apply {
            put("basicAuth.enabled", basicAuthEnabled.toString())
        }
        application.main(DI {
            bind { singleton { environment.config } }
            bind { singleton { ocpp16AuthService } }
            bind { singleton { ocpp16SessionManager } }
        })
        test()
    }
}

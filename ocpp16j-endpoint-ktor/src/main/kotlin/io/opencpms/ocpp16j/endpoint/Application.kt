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

import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.opencpms.ocpp16.service.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import io.opencpms.ocpp16j.endpoint.websocket.configureSockets
import org.kodein.di.*
import java.security.KeyStore

fun main() {
    val appConfig = AppConfig()
    val context = createContext(appConfig)
    val environment = createApplicationEngineEnvironment(appConfig, context)

    embeddedServer(Jetty, environment).start(wait = true)
}

fun createContext(appConfig: AppConfig): DI = DI {
    bind { singleton { appConfig } }

//            TODO: inject
//            bind { singleton { Ocpp16AuthService() } }
    bind { singleton { Ocpp16SessionManager() } }
//            TODO: inject
//            bind { singleton { Ocpp16IncomingMessageService() } }
}


fun createApplicationEngineEnvironment(appConfig: AppConfig, context: DI) = applicationEngineEnvironment {
    if (appConfig.useTls) {
        val tlsConfig = appConfig.tlsConfig!!
        val keyStorePassword = tlsConfig.keyStorePassword.toCharArray()
        sslConnector(
            keyStore = KeyStore.getInstance(
                tlsConfig.keyStoreFile,
                keyStorePassword
            ),
            keyStorePassword = { keyStorePassword },
            keyAlias = tlsConfig.privateKeyAlias,
            privateKeyPassword = { tlsConfig.privateKeyPassword.toCharArray() }
        )
        {
            port = appConfig.port
            host = appConfig.hostname
            keyStorePath = tlsConfig.keyStoreFile
        }
    } else {
        connector {
            port = appConfig.port
            host = appConfig.hostname
        }
    }

    module { configureSockets(context) }
}

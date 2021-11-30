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

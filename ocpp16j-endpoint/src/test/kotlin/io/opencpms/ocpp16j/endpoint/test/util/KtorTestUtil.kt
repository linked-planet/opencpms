package io.opencpms.ocpp16j.endpoint.test.util

import io.ktor.config.*
import io.ktor.server.testing.*
import io.opencpms.ocpp16.service.auth.*
import io.opencpms.ocpp16.service.receiver.*
import io.opencpms.ocpp16.service.session.Ocpp16SessionManager
import io.opencpms.ocpp16j.endpoint.main
import org.kodein.di.*

fun withTestApplication(
    basicAuthEnabled: Boolean,
    ocpp16AuthService: Ocpp16AuthService = Ocpp16AuthServiceImpl(),
    ocpp16SessionManager: Ocpp16SessionManager = Ocpp16SessionManager(),
    ocpp16MessageReceiver: Ocpp16MessageReceiver = Ocpp16MessageReceiverImpl(),
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
            bind { singleton { ocpp16MessageReceiver } }
        })
        test()
    }
}

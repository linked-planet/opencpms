package io.opencpms.ocpp16j.endpoint.websocket

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import io.opencpms.ocpp16.service.Ocpp16AuthService
import io.opencpms.ocpp16j.endpoint.config.AppConfig
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Route::class.java)

fun Route.ocpp16AuthorizedChargePoint(callback: Route.() -> Unit): Route {
    val appConfig by closestDI().instance<AppConfig>()
    val authService by closestDI().instance<Ocpp16AuthService>()

    // Attach to route
    val modifiedRoute = this.createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercept calls from this route at the call step
    modifiedRoute.intercept(ApplicationCallPipeline.Call) {
        val chargePointId = this.call.parameters["chargePointId"]
        val basicAuthActivated = appConfig.useBasicAuth

        log.debug("Requesting authorization [$chargePointId]")
        val isAuthorized = chargePointId
            ?.let {
                when (basicAuthActivated) {
                    false -> {
                        authService.authenticateChargePoint(chargePointId)
                            .fold(
                                ifLeft = {
                                    log.debug("Unknown chargePointId [$chargePointId]")
                                    false
                                },
                                ifRight = { true }
                            )
                    }
                    true -> {
                        call.request.basicAuthenticationCredentials()
                            ?.takeIf { it.name == chargePointId }
                            ?.let {
                                authService.authenticateChargePointWithAuthKey(it.name, it.password)
                                    .fold(
                                        ifLeft = {
                                            log.debug("Provided credentials are invalid [$chargePointId]")
                                            false
                                        },
                                        ifRight = { true }
                                    )
                            }
                            ?: let {
                                log.debug("No or invalid credentials provided [$chargePointId]")
                                false
                            }
                    }
                }
            }
            ?: let {
                log.debug("No chargePointId provided")
                false
            }

        if (isAuthorized) {
            log.debug("GRANTED authorization for chargePointId [$chargePointId]")
            proceed() // Continue processing
        } else {
            log.debug("DENIED authorization for chargePointId [$chargePointId]")
            return@intercept finish() // Abort and return 404
        }
    }

    // Configure this route with the block provided
    callback(modifiedRoute)

    return modifiedRoute
}

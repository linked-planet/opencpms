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
package io.opencpms.ocpp16j.endpoint.websocket

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.auth.basicAuthenticationCredentials
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.opencpms.ocpp16.service.auth.Ocpp16AuthService
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
            log.debug("GRANTED authorization [$chargePointId]")
            proceed() // Continue processing
        } else {
            log.debug("DENIED authorization [$chargePointId]")
            return@intercept finish() // Abort and return 404
        }
    }

    // Configure this route with the block provided
    callback(modifiedRoute)

    return modifiedRoute
}

package io.opencpms.ocpp16.service

class Ocpp16SessionManager {

    private val registeredSessions: MutableMap<String, Ocpp16Session> = mutableMapOf()

    fun registerSession(session: Ocpp16Session) {
        registeredSessions[session.chargePointId] = session
    }

    fun getSession(chargePointId: String): Ocpp16Session? = registeredSessions[chargePointId]

    fun unregisterSession(chargePointId: String) {
        registeredSessions.remove(chargePointId)
    }
}
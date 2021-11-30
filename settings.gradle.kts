rootProject.name = "opencpms"
include("ocpp16-protocol")
include("ocpp16j-endpoint-ktor")
include("ocpp16-service")
include("ocpp201-protocol")

pluginManagement {
    val jvmPluginVersion: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version jvmPluginVersion apply false
    }
}

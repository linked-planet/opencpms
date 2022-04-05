rootProject.name = "opencpms"
include("ocpp16-protocol")
include("ocpp16j-endpoint")
include("ocpp16-service")

pluginManagement {
    val jvmPluginVersion: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version jvmPluginVersion apply false
        id("com.github.hierynomus.license") version "0.16.1" apply false
        id("com.github.ben-manes.versions") version "0.42.0" apply false
        id("io.gitlab.arturbosch.detekt").version("1.19.0") apply false
    }
}

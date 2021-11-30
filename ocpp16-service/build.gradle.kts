plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val arrow_version: String by project
dependencies {
    implementation(project(":ocpp16-protocol"))

    implementation("io.arrow-kt:arrow-core:$arrow_version")
}

group = "io.opencpms"
version = "1.0-SNAPSHOT"

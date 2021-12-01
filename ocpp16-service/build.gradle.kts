plugins {
    kotlin("jvm")
}

val arrow_version: String by project
dependencies {
    implementation(project(":ocpp16-protocol"))

    implementation("io.arrow-kt:arrow-core:$arrow_version")
}

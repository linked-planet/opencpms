plugins {
    kotlin("jvm")
}

val arrowVersion: String by project
dependencies {
    implementation(project(":ocpp16-protocol"))
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
}

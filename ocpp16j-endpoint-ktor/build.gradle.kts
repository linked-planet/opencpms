plugins {
    application
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "io.opencpms"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.opencpms.ocpp16j.endpoint.ApplicationKt")
}

val jvmTarget: String by project

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val arrow_version: String by project
val gson_version: String by project
val koin_version: String by project
val kodein_version: String by project
val mockk_version: String by project
dependencies {
    implementation(project(":ocpp16-service"))
    implementation(project(":ocpp16-protocol"))

    // Kotlin
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-client-core:$ktor_version")
    testImplementation("io.ktor:ktor-client-cio:$ktor_version")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrow_version")

    // Gson
    implementation("com.google.code.gson:gson:$gson_version")

    // Kodein
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodein_version")

    // Mockk
    testImplementation("io.mockk:mockk:$mockk_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
}
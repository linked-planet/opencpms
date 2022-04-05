import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// Configure integration tests
sourceSets {
    create("integrationTest") {
        java.srcDirs("src/integrationTest/kotlin")
        resources.srcDirs("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

// JVM
val jvmTarget: String by project

// Deps
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val arrow_version: String by project
val gson_version: String by project
val koin_version: String by project
val kodein_version: String by project
val mockk_version: String by project
val jackson_version: String by project
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
    implementation("io.ktor:ktor-server-netty:$ktor_version")
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

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

    // Mockk
    testImplementation("io.mockk:mockk:$mockk_version")

    // Docile-Charge-Point
    integrationTestImplementation("com.infuse-ev:docile-charge-point-loader_2.12:0.6.0")
}

// Tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
}

tasks {
    register<Test>("integrationTest") {
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        testLogging {
            events(TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        useJUnit()
    }

    test {
        testLogging {
            events(TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}

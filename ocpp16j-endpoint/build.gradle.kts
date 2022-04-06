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
val kotlinVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val arrowVersion: String by project
val gsonVersion: String by project
val kodeinVersion: String by project
val jacksonVersion: String by project
val mockkVersion: String by project
dependencies {
    implementation(project(":ocpp16-service"))
    implementation(project(":ocpp16-protocol"))

    // Kotlin
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-core:$ktorVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    // Gson
    implementation("com.google.code.gson:gson:$gsonVersion")

    // Kodein
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Mockk
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Docile-Charge-Point
    integrationTestImplementation("com.infuse-ev:docile-charge-point-loader_2.12:0.6.0")
}

// Tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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

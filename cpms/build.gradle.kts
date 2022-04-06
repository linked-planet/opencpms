import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    application
}


//region kotlin
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    val jvmTarget: String by project
    kotlinOptions.jvmTarget = jvmTarget
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}
//endregion


//region unit-tests
tasks.test {
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
//endregion


//region dependencies
val kotlinVersion: String by project
val ktorVersion: String by project
val log4jVersion: String by project
val arrowVersion: String by project
val gsonVersion: String by project
val kodeinVersion: String by project
val jacksonVersion: String by project
val mockkVersion: String by project
dependencies {
    implementation(project(":ktor-rabbitmq"))
    implementation(project(":ocpp16-protocol"))

    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.apache.logging.log4j", "log4j-api", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("com.lmax", "disruptor", "3.4.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}
//endregion
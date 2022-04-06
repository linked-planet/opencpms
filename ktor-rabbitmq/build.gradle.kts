import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    `java-library`
}


//region kotlin
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
val ktorVersion: String by project
val amqpClientVersion: String by project
dependencies {
    api("io.ktor", "ktor-server-core", ktorVersion)
    api("com.rabbitmq", "amqp-client", amqpClientVersion)
}
//endregion

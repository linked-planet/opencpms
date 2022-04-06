println("Gradle Version: " + GradleVersion.current().toString())
println("Java Version: " + JavaVersion.current().toString())

plugins {
    kotlin("jvm") apply false
    id("com.github.hierynomus.license")
    id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
}

allprojects {
    group = "io.opencpms"
    version = "1.0-SNAPSHOT"
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    val jvmTarget: String by project
    ext.set("jvmTarget", jvmTarget)

    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        config = files(rootProject.file("detekt-config.yml"))
    }

    apply(plugin = "com.github.hierynomus.license")
    license {
        header = rootProject.file("LICENSE-HEADER.txt")
        strictCheck = true

        exclude("**/*.properties")
        exclude("**/*.json")

        ext["year"] = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        ext["owner"] = "linked-planet GmbH"
        ext["email"] = "info@linked-planet.com"
    }
}

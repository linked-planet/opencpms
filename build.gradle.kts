import java.util.Calendar

plugins {
    kotlin("jvm") apply false
    id("com.github.hierynomus.license")
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

    ext.set("jvmTarget", "11")

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

        ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
        ext["owner"] = "linked-planet GmbH"
        ext["email"] = "info@linked-planet.com"
    }
}

import java.util.*

plugins {
    kotlin("jvm") apply false
    id("com.github.hierynomus.license")
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

    ext.set("jvmTarget", "1.8")

    apply(plugin = "license")
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

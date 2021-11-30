package util

import io.ktor.network.tls.certificates.*
import java.io.File

fun main() {
    val keyStoreFile = File("build/keystore.jks")
    generateCertificate(
        file = keyStoreFile,
        jksPassword = "bar",
        keyAlias = "sample",
        keyPassword = "foo"
    )
}

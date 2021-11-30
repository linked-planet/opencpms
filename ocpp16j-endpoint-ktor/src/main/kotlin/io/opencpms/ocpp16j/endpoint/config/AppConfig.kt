package io.opencpms.ocpp16j.endpoint.config

import com.typesafe.config.ConfigFactory
import java.io.File

class AppConfig {
    private val appConfig = ConfigFactory.load().getConfig("app")
    val hostname: String = appConfig.getString("hostname")
    val port: Int = appConfig.getInt("port")

    private val basicAuthConfig = appConfig.getConfig("basicAuth")
    val useBasicAuth = basicAuthConfig.getBoolean("enabled")

    private val tlsConfigBlock = appConfig.getConfig("tls")
    val useTls: Boolean = tlsConfigBlock.getBoolean("enabled")
    val tlsConfig: TlsConfig? = if (useTls) {
        val keyStoreFile: File = tlsConfigBlock.getString("keyStorePath")
            ?.let { File(it) }
            ?.takeIf { it.exists() && it.isFile }
            ?: throw IllegalArgumentException("Specified keystore not existing")
        val keyStorePassword: String = tlsConfigBlock.getString("keyStorePassword")
        val privateKeyAlias: String = tlsConfigBlock.getString("privateKeyAlias")
        val privateKeyPassword: String = tlsConfigBlock.getString("privateKeyPassword")
        TlsConfig(keyStoreFile, keyStorePassword, privateKeyAlias, privateKeyPassword)
    } else null
}

/**
 * OpenCPMS
 * Copyright (C) 2021 linked-planet GmbH (info@linked-planet.com).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.opencpms.ocpp16j.endpoint.config

import com.typesafe.config.ConfigFactory
import java.io.File

class AppConfig {
    private val appConfigBlock = ConfigFactory.load().getConfig("app")
    val hostname: String = appConfigBlock.getString("hostname")
    val port: Int = appConfigBlock.getInt("port")

    private val basicAuthConfigBlock = appConfigBlock.getConfig("basicAuth")
    val useBasicAuth = basicAuthConfigBlock.getBoolean("enabled")

    private val tlsConfigBlock = appConfigBlock.getConfig("tls")
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

    data class TlsConfig(
        val keyStoreFile: File,
        val keyStorePassword: String,
        val privateKeyAlias: String,
        val privateKeyPassword: String
    )
}

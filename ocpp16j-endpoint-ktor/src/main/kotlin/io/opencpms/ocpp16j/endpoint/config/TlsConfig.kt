package io.opencpms.ocpp16j.endpoint.config

import java.io.File

data class TlsConfig(
    val keyStoreFile: File,
    val keyStorePassword: String,
    val privateKeyAlias: String,
    val privateKeyPassword: String
)
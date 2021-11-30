package util

import java.util.*

fun encodeBase64(str: String): String = Base64.getEncoder().encodeToString(str.toByteArray())

fun decodeBase64(bytes: ByteArray): String = Base64.getDecoder().decode(bytes).toString()
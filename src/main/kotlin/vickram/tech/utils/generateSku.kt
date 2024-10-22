package vickram.tech.utils

import java.util.*

fun generateRandomSku(length: Int = 8): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun String.toUUID(): UUID {
    return UUID.fromString(this)
}


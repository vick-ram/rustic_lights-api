package vickram.tech.utils

import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File

fun saveImage(part: PartData.FileItem, fileDir: String): String? {
    val fileName = part.originalFileName ?: return null
    val file = File(fileDir, fileName)
    if (file.exists()) {
        file.delete()
    }
    val byteReadChannel = part.provider()
    try {
        byteReadChannel.toInputStream().use { inputStream ->
            file.outputStream().buffered().use {
                inputStream.copyTo(it)
            }
        }
    } finally {
        byteReadChannel.cancel()
    }
    return "images/$fileName"
}
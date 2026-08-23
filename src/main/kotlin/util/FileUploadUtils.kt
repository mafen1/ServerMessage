package com.example.util

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import java.io.File
import java.util.UUID

private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private val ALLOWED_IMAGE_CONTENT_TYPES = setOf(
    ContentType.Image.JPEG,
    ContentType.Image.PNG,
    ContentType("image", "webp")
)
const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB

fun PartData.FileItem.isValidImage(): Boolean {
    val ext = extension().lowercase()
    if (ext !in ALLOWED_IMAGE_EXTENSIONS) return false

    val contentType = this.contentType ?: return false
    return contentType in ALLOWED_IMAGE_CONTENT_TYPES
}

fun PartData.FileItem.extension(): String =
    originalFileName
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.takeIf { it.isNotBlank() && it.length <= 8 }
        ?: "jpg"

fun PartData.FileItem.safeImageFileName(prefix: String = "file"): String {
    val ext = extension().lowercase()
    val validatedExt = if (ext in ALLOWED_IMAGE_EXTENSIONS) ext else "jpg"
    return "${prefix}_${UUID.randomUUID()}.$validatedExt"
}

suspend fun PartData.FileItem.copyToFileWithLimit(file: File, limit: Long = MAX_IMAGE_SIZE_BYTES) {
    val channel = provider()
    file.outputStream().use { output ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (!channel.isClosedForRead) {
            val read = channel.readAvailable(buffer)
            if (read <= 0) break
            if (total + read > limit) {
                file.delete()
                throw IllegalArgumentException("File too large. Max size is $limit bytes")
            }
            output.write(buffer, 0, read)
            total += read
        }
    }
}

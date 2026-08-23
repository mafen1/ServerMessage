package com.example.message

import com.example.message.model.ImageUploadResponse
import com.example.message.repository.MessageRepository
import com.example.util.MAX_IMAGE_SIZE_BYTES
import com.example.util.copyToFileWithLimit
import com.example.util.isValidImage
import com.example.util.safeImageFileName
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import java.io.File

fun Application.MessageRouting(messageRepo: MessageRepository = get()) {
    routing {
        authenticate("jwt") {
            get("/messages/{user1}/{user2}") {
                try {
                    val user1 = call.parameters["user1"]
                    val user2 = call.parameters["user2"]

                    if (user1.isNullOrBlank() || user2.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Usernames не указаны"))
                        return@get
                    }

                    // читать историю может только участник чата
                    val caller = call.principal<JWTPrincipal>()
                        ?.payload?.getClaim("userName")?.asString()?.takeIf { it.isNotBlank() }
                    if (caller == null || (caller != user1 && caller != user2)) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You are not a participant of this chat"))
                        return@get
                    }

                    val messages = messageRepo.getMessagesBetweenUsers(user1, user2)
                    call.respond(HttpStatusCode.OK, messages)
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to load messages", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            rateLimit(RateLimitName("upload")) {
                post("/uploadMessageImage") {
                    try {
                        // без multipart-тела сразу 400, а не 500 из глубины receiveMultipart
                        if (!call.request.contentType().match(ContentType.MultiPart.FormData)) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "multipart/form-data body is required"))
                            return@post
                        }
                        val imagesDirectory = File("images").apply { mkdirs() }
                        val multipartData = call.receiveMultipart(formFieldLimit = MAX_IMAGE_SIZE_BYTES)
                        var fileName = ""

                        multipartData.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                if (!part.isValidImage()) {
                                    part.dispose()
                                    throw IllegalArgumentException("Invalid image file")
                                }
                                fileName = part.safeImageFileName("chat")
                                val targetFile = File(imagesDirectory, fileName)
                                part.copyToFileWithLimit(targetFile, MAX_IMAGE_SIZE_BYTES)
                            }
                            part.dispose()
                        }

                        if (fileName.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Image is required"))
                            return@post
                        }

                        call.respond(HttpStatusCode.OK, ImageUploadResponse(fileName))
                    } catch (e: IllegalArgumentException) {
                        call.application.environment.log.warn("Image upload failed: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                    } catch (e: Exception) {
                        call.application.environment.log.error("Failed to upload message image", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                    }
                }
            }
        }
    }
}

package com.example.news

import com.example.login.Login
import com.example.news.model.CommentRequest
import com.example.news.model.LikeRequest
import com.example.news.model.NewsRequest
import com.example.news.repository.News
import com.example.util.MAX_IMAGE_SIZE_BYTES
import com.example.util.copyToFileWithLimit
import com.example.util.isValidImage
import com.example.util.safeImageFileName
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import java.io.File

fun Application.routingNews(
    newsImpl: News = get(),
    loginImpl: Login = get()
) {

    val imagesDirectory = File("images").apply { mkdirs() }

    routing {

        authenticate("jwt") {
            get("/allNews") {
                try {
                    call.respond(newsImpl.allNews())
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to load news", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            post("/news/like") {
                try {
                    val tokenUser = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    val request = call.receive<LikeRequest>()
                    if (tokenUser != request.userName) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "userName mismatch"))
                        return@post
                    }
                    call.respond(HttpStatusCode.OK, newsImpl.toggleLike(request.newsId, request.userName))
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to toggle like", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "like failed"))
                }
            }

            post("/news/comment") {
                try {
                    val tokenUser = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    val request = call.receive<CommentRequest>()
                    if (request.text.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "comment is blank"))
                        return@post
                    }
                    if (tokenUser != request.userName) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "userName mismatch"))
                        return@post
                    }

                    call.respond(HttpStatusCode.OK, newsImpl.addComment(request.newsId, request.userName, request.text))
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to add comment", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "comment failed"))
                }
            }

            rateLimit(RateLimitName("upload")) {
                post("/uploadNewsWithOutImage") {
                    try {
                        val tokenUser = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                        val newsWithOutImage = call.receive<NewsRequest>()
                        if (tokenUser != newsWithOutImage.userNameAuthor) {
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "author mismatch"))
                            return@post
                        }
                        newsImpl.uploadNewsWithOutImage(newsWithOutImage)
                        call.respond(HttpStatusCode.OK, mapOf("message" to "News saved successfully"))
                    } catch (e: Exception) {
                        call.application.environment.log.error("Failed to upload news without image", e)
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "news save failed"))
                    }
                }
            }
        }

        get("/images/{name}") {
            val imageName = call.parameters["name"]
            if (imageName.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Image name is required"))
                return@get
            }

            if (imageName.contains("..") || imageName.contains("/") || imageName.contains("\\")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid image name"))
                return@get
            }

            val token = call.request.queryParameters["token"]
                ?: call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            if (token.isNullOrBlank() || !loginImpl.validateToken(token)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                return@get
            }

            val file = File(imagesDirectory, imageName)
            if (!file.exists() || !file.isFile) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Image not found"))
                return@get
            }

            call.respondFile(file)
        }

        authenticate("jwt") {
            rateLimit(RateLimitName("upload")) {
                post("/uploadNews") {
                    try {
                        // без multipart-тела сразу 400, а не 500 из глубины receiveMultipart
                        if (!call.request.contentType().match(ContentType.MultiPart.FormData)) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "multipart/form-data body is required"))
                            return@post
                        }
                        val tokenUser = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                        val multipartData = call.receiveMultipart(formFieldLimit = MAX_IMAGE_SIZE_BYTES)

                        var fileName = ""
                        var newsRequest: NewsRequest? = null

                        multipartData.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    if (part.name == "NewsRequest") {
                                        try {
                                            newsRequest = Json.decodeFromString(part.value)
                                        } catch (e: Exception) {
                                            call.application.environment.log.warn("Failed to parse NewsRequest", e)
                                        }
                                    }
                                }
                                is PartData.FileItem -> {
                                    if (!part.isValidImage()) {
                                        part.dispose()
                                        throw IllegalArgumentException("Invalid image file")
                                    }
                                    fileName = part.safeImageFileName("news")
                                    val targetFile = File(imagesDirectory, fileName)
                                    part.copyToFileWithLimit(targetFile, MAX_IMAGE_SIZE_BYTES)
                                }
                                else -> {}
                            }
                            part.dispose()
                        }

                        val request = newsRequest
                        if (request == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing NewsRequest field"))
                            return@post
                        }
                        if (tokenUser != request.userNameAuthor) {
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "author mismatch"))
                            return@post
                        }

                        newsImpl.addNews(request, fileName)
                        call.respond(HttpStatusCode.OK, mapOf("message" to "News uploaded successfully"))
                    } catch (e: IllegalArgumentException) {
                        call.application.environment.log.warn("News upload failed: ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                    } catch (e: Exception) {
                        call.application.environment.log.error("Failed to upload news", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                    }
                }
            }
        }
    }
}

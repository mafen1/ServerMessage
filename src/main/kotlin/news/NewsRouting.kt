package com.example.news

import com.example.news.model.NewsRequest
import com.example.news.repository.NewsImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.io.File

fun Application.routingNews() {

    install(CallLogging) {
        level = Level.INFO

    }

    routing {

        static(
            "/images"
        ) {
            files("images")
        }

        post("/uploadNews") {
            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10)

            var fileName = ""
            var newsRequest: NewsRequest? = null

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "NewsRequest") {
                            try {
                                newsRequest = Json.decodeFromString(part.value)
                                println("✓ Parsed NewsRequest: $newsRequest")
                            } catch (e: Exception) {
                                println("✗ Failed to parse NewsRequest: ${e.message}")
                            }
                        }
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "unknown.jpg"
                        val file = File("images/${fileName}")
                        part.provider().copyAndClose(file.writeChannel())
                        println("✓ Saved file: ${file.absolutePath}")
                    }
                    else -> {}
                }
            }

            try {
                if (newsRequest != null) {
                    NewsImpl().addNews(newsRequest!!, fileName)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "News uploaded successfully"))
                } else {
                    println("✗ newsRequest is null!")
                    call.respond(HttpStatusCode.BadRequest, "Missing NewsRequest field")
                }
            } catch (e: Exception) {
                println("✗ Exception: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }

        get("/allNews") {

            try {
                val newsImpl = NewsImpl()
                call.respond(newsImpl.allNews())
            } catch (e: Exception) {
                println("All news ${e.toString()}")
            }
        }

//        post("/uploadNewsWithOutImage") {
//            val newsWithOutImage = call.receive<NewsWithOutImage>()
//            println(newsWithOutImage.toString())
//            try {
//                NewsImpl().uploadNewsWithOutImage(newsWithOutImage)
//                call.respond(HttpStatusCode.OK, "news save")
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.BadRequest, "news save failed ${e.toString()}")
//                println(e.toString())
//            }
//        }


    }


}

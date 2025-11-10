package com.example.news

import com.example.news.model.NewsRequest
import com.example.news.model.NewsWithOutImage
import com.example.news.repository.NewsImpl
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.files
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File
import java.util.Random
import java.util.logging.Level

fun Application.routingNews() {

    install(CallLogging){
       level = org.slf4j.event.Level.INFO
    }

    routing {

        static(
            "/images"){
            files("images")
        }

        post("/uploadNews") {
            var fileDescription = ""
            var fileName = ""

            var userName: String? = ""
            var desctiption = ""


            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10)

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "userName") {
                            userName = part.value
                        }
                        if (part.name == "nameNews"){
                            desctiption = part.value
                        }
                    }

                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val file = File("images/${fileName}.jpg")
                        part.provider().copyAndClose(file.writeChannel())
                        println(file.toString())

                    }

                    else -> {}
                }

                NewsImpl().addNews(NewsRequest(
                    id = Random().nextInt(),
                    userName = userName!!,
                    image = fileName,
                    text = desctiption
                ))
                part.dispose()

            }

            call.respondText("$fileDescription is uploaded to 'uploads/$fileName'")
        }

        get("/allNews") {
            val newsImpl = NewsImpl()
            call.respond(newsImpl.allNews())
        }

        post("/uploadNewsWithOutImage") {
            try {
                val newsWithOutImage = call.receive<NewsWithOutImage>()
                val newsImpl = NewsImpl()

                newsImpl.uploadNewsWithOutImage(newsWithOutImage)

                call.respond(HttpStatusCode.OK, "news save")
            } catch (e: Exception) {
               call.respond(HttpStatusCode.BadRequest, "news save failed ${e.toString()}")
            }
        }


    }


}

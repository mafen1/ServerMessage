package com.example

import com.example.data.model.Message
import com.example.data.repository.MessageRepoImpl
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }



    routing {
        get("/test") {

        }
        post("/message") {
            try {
                val message = call.receive<Message>()
                MessageRepoImpl().addMessageToDB(message.id, message.name, message.message)
                call.respondText("Данные сохранены в базу данных: $message", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText("Ошибка: ${e.message}", status = HttpStatusCode.BadRequest)
            }

        }

    }
}


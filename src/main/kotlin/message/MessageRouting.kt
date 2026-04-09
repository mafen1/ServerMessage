package com.example.message

import com.example.message.repository.MessageRepoImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.MessageRouting() {

    val messageRepo = MessageRepoImpl()

    routing {
        get("/messages/{user1}/{user2}") {
            try {
                val user1 = call.parameters["user1"]
                val user2 = call.parameters["user2"]

                if (user1.isNullOrBlank() || user2.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Usernames не указаны"))
                    return@get
                }

                val messages = messageRepo.getMessagesBetweenUsers(user1, user2)
                call.respond(HttpStatusCode.OK, messages)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
            }
        }
    }
}

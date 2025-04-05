package com.example.data.user

import com.example.data.user.model.UserRequest
import com.example.data.user.repository.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.UserRouting() {
    routing {
        post("/findUserByName") {
            try {
                val request: UserRequest = call.receive()
                val user = UserRepositoryImpl().findUserByName(request)
                call.respond(user)

            }catch (e: Exception){
                call.respond(e.message.toString())
            }
        }
    }
}
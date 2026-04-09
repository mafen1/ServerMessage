package com.example.user

import com.example.user.model.UserRequest
import com.example.user.repository.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.UserRouting() {

    routing {
        get("/allUser") {
            call.respond(UserRepositoryImpl().allUser())
        }
        post("/findUserByStr") {
            val str = call.receive<UserRequest>()
            val users = UserRepositoryImpl().findUserByStr(str)
            call.respond(users)
        }
    }

}
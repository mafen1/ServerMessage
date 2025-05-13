package com.example.user

import com.example.user.model.UserRequest
import com.example.user.repository.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.UserRouting() {
    routing {
        post("/findUserByName") {
            try {
                val request: UserRequest = call.receive()
                val user = UserRepositoryImpl().findUserByUserName(request)
                call.respond(user)

            }catch (e: Exception){
                call.respond(e.message.toString())
            }
        }
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
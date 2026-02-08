package com.example.user

import com.example.login.model.LoginResponse
import com.example.user.model.User
import com.example.user.model.UserRequest
import com.example.user.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.UserRouting() {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    routing {

        post("/register") {
            try {
                val user = call.receive<User>()
                UserRepositoryImpl().addUser(user)

                // Создаем успешный ответ с токеном и пользователем
                val token = "temp_token_${user.username}"
                val loginResponse = LoginResponse(
                    token = token,
                    expiresAt = "2024-12-31T23:59:59Z",
                    user = user.copy(token = token)
                )
                call.respond(HttpStatusCode.Created, loginResponse)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
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
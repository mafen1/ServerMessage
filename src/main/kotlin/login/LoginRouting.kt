package com.example.login

import com.example.login.model.LoginRequest
import com.example.login.model.TokenRequest
import com.example.user.model.User
import com.example.user.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.LoginRouting() {

    val loginImpl = LoginImpl()

    routing {

        post("/register") {

            try {
                val user = call.receive<User>()
                val loginResponse = loginImpl.createJWT(user)

                UserRepositoryImpl().addUser(
                    User(
                        id = user.id,
                        name = user.name,
                        userName = user.userName,
                        friend = listOf(),
                        token = loginResponse.token
                    )
                )
                call.respond(loginResponse)
            } catch (e: Exception) {
                call.respondText("Ошибка ${e.message}")
            }
        }

        post("/addFriend") {
            UserRepositoryImpl().addFriends("@gjdfs")
        }

        // todo Протестить
        post("/login") {
            val loginRequest: LoginRequest = call.receive()
            println(loginRequest.toString())
            if (loginImpl.validateUserByUserName(loginRequest.username)) {
                val user = UserRepositoryImpl().findUserUserName(loginRequest.username)
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
            }
        }

        post("/findUserToken") {
            try {
                val request: TokenRequest = call.receive()
                val token = request.token

                // если находим пользователя по токену отправляем пользователя
                if (loginImpl.validateToken(token)) {
                    call.respond(
                        HttpStatusCode.OK,
                        UserRepositoryImpl().findUserToken(token)
                    )
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
                }
            } catch (e: Exception) {
                call.respondText("Ошибка ${e.message}")
            }
        }
    }
}

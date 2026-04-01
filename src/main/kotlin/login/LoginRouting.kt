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
        // TODO обрабатывать ошибки
        post("/register") {

            try {
                val user = call.receive<User>()
                val loginResponse = loginImpl.createJWT(user)

                // id будет сгенерирован автоматически базой данных
                UserRepositoryImpl().addUser(
                    User(
                        id = 0, // 0 означает, что ID сгенерируется БД
                        name = user.name,
                        username = user.username,
                        listUserName = listOf(),
                        token = loginResponse.token,
                        password = user.password
                    )
                )

                println(user)
                call.respond(loginResponse)
            } catch (e: Exception) {
                call.respondText("Ошибка ${e.message}")
            }
        }

        post("/addFriend") {
            UserRepositoryImpl().addFriends("@gjdfs")
        }

        post("/login") {
            try {
                val loginRequest: LoginRequest = call.receive()
                if (loginImpl.validateUserByUserName(loginRequest.username)) {

                    val user = UserRepositoryImpl().findUserUserName(loginRequest.username)
                    call.respond(HttpStatusCode.OK, user)

                } else {
                    call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
                }
            } catch (e: Exception) {
                println(e.toString())
            }
        }

        post("/getUserToken") {
            try {
                val request: TokenRequest = call.receive()
                val token = request.token

                // если находим пользователя по токену отправляем пользователя
                if (loginImpl.validateToken(token)) {
                    call.respond(
                        HttpStatusCode.OK,
                        UserRepositoryImpl().findUserToken(token)
                    )
                    println("token validate")

                } else {
                    call.respond(HttpStatusCode.BadRequest, "Пользователь не найден")
                }
            } catch (e: Exception) {
                call.respondText("Ошибка ${e.message}")
            }
        }
    }
}

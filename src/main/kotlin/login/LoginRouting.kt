package com.example.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.login.model.LoginResponse
import com.example.login.model.TokenRequest
import com.example.user.model.User
import com.example.user.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

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


        post("/login") {
            val user: User = call.receive()

            if (loginImpl.validateUser(user)) {
                UserRepositoryImpl().findUser(user)
                loginImpl.createJWT(user)
                call.respond(HttpStatusCode.OK, loginImpl.createJWT(user))
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

package com.example.login

import com.example.login.model.LoginRequest
import com.example.login.model.RegisterRequest
import com.example.user.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.LoginRouting(
    loginImpl: Login = get(),
    userRepository: UserRepository = get()
) {

    routing {
        rateLimit(RateLimitName("auth")) {
            post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                if (request.name.isBlank() || request.userName.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Заполните имя, username и пароль"))
                    return@post
                }
                if (userRepository.existsByUserName(request.userName)) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "Пользователь уже существует"))
                    return@post
                }

                val loginResponse = loginImpl.createJWT(
                    com.example.user.model.User(
                        name = request.name,
                        userName = request.userName,
                        listUserName = emptyList()
                    )
                )

                userRepository.addUser(
                    name = request.name,
                    userName = request.userName,
                    password = request.password
                )

                call.respond(HttpStatusCode.Created, loginResponse)
            } catch (e: IllegalArgumentException) {
                call.application.environment.log.warn("Registration failed: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
            } catch (e: Exception) {
                call.application.environment.log.error("Registration failed", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }

        post("/login") {
            try {
                val loginRequest: LoginRequest = call.receive()
                if (loginRequest.userName.isBlank() || loginRequest.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username и пароль обязательны"))
                    return@post
                }

                val loginResponse = loginImpl.loginAccount(loginRequest)
                call.respond(HttpStatusCode.OK, loginResponse)
            } catch (e: IllegalArgumentException) {
                call.application.environment.log.warn("Login failed: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Неверный username или пароль"))
            } catch (e: Exception) {
                call.application.environment.log.error("Login failed", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }

        }

        authenticate("jwt") {
            get("/me") {
                try {
                    val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
                    val userName = principal?.payload?.getClaim("userName")?.asString()
                    if (userName.isNullOrBlank()) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val user = userRepository.findUserUserName(userName)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to fetch current user", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }
        }
    }
}

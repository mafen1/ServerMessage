package com.example.user

import com.example.user.model.UpdateProfileRequest
import com.example.user.model.UserRequest
import com.example.user.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.UserRouting(userRepository: UserRepository = get()) {

    routing {
        authenticate("jwt") {
            get("/allUser") {
                try {
                    call.respond(userRepository.allUser())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to fetch users")))
                }
            }
            post("/findUserByStr") {
                try {
                    val str = call.receive<UserRequest>()
                    val users = userRepository.findUserByStr(str)
                    call.respond(users)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to search users")))
                }
            }
            post("/findUserByName") {
                try {
                    val request = call.receive<UserRequest>()
                    call.respond(userRepository.findUserByUserName(request))
                } catch (e: IllegalArgumentException) {
                    // пользователь не найден — это 404, а не ошибка запроса
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "User not found")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to find user")))
                }
            }
            post("/updateProfile") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    val request = call.receive<UpdateProfileRequest>()
                    if (tokenUser != request.userName) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden: username mismatch"))
                        return@post
                    }
                    call.respond(userRepository.updateProfile(request.userName, request.name, request.password))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Failed to update profile")))
                }
            }
        }
    }

}

package com.example.security

import com.example.message.repository.MessageRepository
import com.example.security.model.PublicKeyRequest
import com.example.security.model.WrappedKeyRequest
import com.example.user.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.KeyRouting(
    userRepository: UserRepository = get(),
    messageRepository: MessageRepository = get()
) {
    routing {
        authenticate("jwt") {
            post("/keys") {
                val userName = call.extractUserName()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, unauthorizedResponse())

                val request = call.receive<PublicKeyRequest>()
                if (request.publicKey.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "publicKey is required"))
                }

                try {
                    userRepository.updatePublicKey(userName, request.publicKey)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "User not found")))
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to update public key", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            get("/keys/{username}") {
                val username = call.parameters["username"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username is required"))

                val publicKey = userRepository.getPublicKey(username)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Public key not found"))

                call.respond(HttpStatusCode.OK, mapOf("publicKey" to publicKey))
            }

            post("/chat-keys") {
                val userName = call.extractUserName()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, unauthorizedResponse())

                val request = call.receive<WrappedKeyRequest>()
                if (request.chatId.isBlank() || request.recipientUsername.isBlank() || request.wrappedKey.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "chatId, recipientUsername and wrappedKey are required"))
                }

                try {
                    messageRepository.saveWrappedChatKey(request.chatId, request.recipientUsername, request.wrappedKey)
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to save wrapped chat key", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }

            get("/chat-keys/{chatId}") {
                val userName = call.extractUserName()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, unauthorizedResponse())

                val chatId = call.parameters["chatId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "chatId is required"))
                val recipient = call.request.queryParameters["recipient"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "recipient query parameter is required"))

                val wrappedKey = messageRepository.getWrappedChatKey(chatId, recipient)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wrapped key not found"))

                call.respond(HttpStatusCode.OK, mapOf("chatId" to chatId, "wrappedKey" to wrappedKey))
            }
        }
    }
}

private fun ApplicationCall.extractUserName(): String? {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("userName")?.asString()?.takeIf { it.isNotBlank() }
}

private fun unauthorizedResponse() = mapOf("error" to "Unauthorized")

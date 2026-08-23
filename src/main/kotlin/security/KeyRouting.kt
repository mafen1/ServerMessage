package com.example.security

import com.example.message.model.WrappedKeyEntry
import com.example.message.repository.MessageRepository
import com.example.security.model.PublicKeyRequest
import com.example.security.model.PublishChatKeysRequest
import com.example.security.model.PublishChatKeysResponse
import com.example.security.model.WrappedKeyRequest
import com.example.security.model.WrappedKeyWithVersionResponse
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

        post("/chat-keys/publish") {
            val userName = call.extractUserName()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, unauthorizedResponse())

            val request = call.receive<PublishChatKeysRequest>()
            if (request.chatId.isBlank() || request.entries.isEmpty()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "chatId and non-empty entries are required"))
            }

            // публиковать может только участник чата и только для его участников
            // (копия для себя — отдельный случай: recipient совпадает с вызывающим)
            val validEntries = request.entries.map { entry ->
                val expectedChatId = listOf(userName, entry.recipientUsername).sorted().joinToString("__")
                Triple(entry.recipientUsername, entry.wrappedKey, expectedChatId)
            }
            if (validEntries.any { (recipient, wrappedKey, expectedChatId) ->
                    wrappedKey.isBlank() || (recipient != userName && expectedChatId != request.chatId)
                }
            ) {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "chatId does not match participants"))
            }

            try {
                val version = messageRepository.publishWrappedChatKeys(
                    request.chatId,
                    validEntries.map { WrappedKeyEntry(it.first, it.second) }
                )
                call.respond(HttpStatusCode.OK, PublishChatKeysResponse(success = true, version = version))
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to publish chat keys", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }

        // legacy-роут для старых клиентов: одиночный апсерт через ту же эпоху
        post("/chat-keys") {
            val userName = call.extractUserName()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, unauthorizedResponse())

            val request = call.receive<WrappedKeyRequest>()
            if (request.chatId.isBlank() || request.recipientUsername.isBlank() || request.wrappedKey.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "chatId, recipientUsername and wrappedKey are required"))
            }
            val expectedChatId = listOf(userName, request.recipientUsername).sorted().joinToString("__")
            if (request.recipientUsername != userName && expectedChatId != request.chatId) {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "chatId does not match participants"))
            }

            try {
                val version = messageRepository.publishWrappedChatKeys(
                    request.chatId,
                    listOf(WrappedKeyEntry(request.recipientUsername, request.wrappedKey))
                )
                call.respond(HttpStatusCode.OK, PublishChatKeysResponse(success = true, version = version))
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
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("recipient query parameter is required" to true))

            // читать можно только собственную копию ключа
            if (recipient != userName) {
                return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "recipient must be the authenticated user"))
            }

            val wrappedKey = messageRepository.getWrappedChatKey(chatId, recipient)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wrapped key not found"))

            call.respond(
                HttpStatusCode.OK,
                WrappedKeyWithVersionResponse(
                    chatId = wrappedKey.chatId,
                    wrappedKey = wrappedKey.wrappedKey,
                    version = wrappedKey.version
                )
            )
            }
        }
    }
}

private fun ApplicationCall.extractUserName(): String? {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("userName")?.asString()?.takeIf { it.isNotBlank() }
}

private fun unauthorizedResponse() = mapOf("error" to "Unauthorized")

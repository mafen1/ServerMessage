package com.example.message

import com.auth0.jwt.JWT
import com.example.friend.repository.Friend
import com.example.login.Login
import com.example.message.repository.MessageRepository
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.seconds

private const val MAX_WEBSOCKET_FRAME_SIZE = 8 * 1024 * 1024L

fun Application.configureSockets(
    messageRepo: MessageRepository = get(),
    loginImpl: Login = get(),
    webSocketManager: WebSocketManager = get(),
    friendRepo: Friend = get()
) {
    install(WebSockets) {

        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = MAX_WEBSOCKET_FRAME_SIZE
        masking = false

    }

    routing {
        webSocket("/chat/{username}") {
            val userName = call.parameters["username"]
            if (userName.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Username is required"))
                return@webSocket
            }

            val token = call.request.queryParameters["token"]
                ?: call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            if (token.isNullOrBlank() || !loginImpl.validateToken(token)) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
                return@webSocket
            }
            val tokenUserName = try { JWT.decode(token).getClaim("userName").asString() } catch (_: Exception) { null }
            if (tokenUserName != userName) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Username mismatch"))
                return@webSocket
            }

            webSocketManager.addSession(userName, this)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val parts = frame.readText().split(":", limit = 5)
                        try {
                            // доставку разрешаем только друзьям: защита от спама в произвольные чаты
                            if (parts.size >= 3) {
                                val toUsername = parts[1]
                                if (!friendRepo.isAlreadyFriends(userName, toUsername)) {
                                    outgoing.send(Frame.Text("error:${toUsername} is not in your friends list"))
                                    continue
                                }
                            }
                            if (parts.size >= 5 && parts[0] == "to") {
                                val toUsername = parts[1]
                                val messageType = parts[2].ifBlank { "text" }
                                val clientMessageId = parts[3]
                                val textMessage = parts[4]
                                val chatId = listOf(userName, toUsername).sorted().joinToString("__")

                                messageRepo.addMessageToDB(userName, toUsername, textMessage, messageType, chatId, clientMessageId)
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, toUsername, messageType, clientMessageId, textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, toUsername, messageType, clientMessageId, textMessage)
                            } else if (parts.size >= 4) {
                                val toUsername = parts[1]
                                val messageType = parts[2].ifBlank { "text" }
                                val textMessage = parts[3]
                                val chatId = listOf(userName, toUsername).sorted().joinToString("__")

                                messageRepo.addMessageToDB(userName, toUsername, textMessage, messageType, chatId, "")
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, toUsername, messageType, "", textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, toUsername, messageType, "", textMessage)
                            } else if (parts.size >= 3) {
                                val toUsername = parts[1]
                                val textMessage = parts[2]
                                val chatId = listOf(userName, toUsername).sorted().joinToString("__")

                                messageRepo.addMessageToDB(userName, toUsername, textMessage, "text", chatId, "")
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, toUsername, "text", "", textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, toUsername, "text", "", textMessage)
                            }
                        } catch (e: Exception) {
                            call.application.environment.log.error("Failed to process WebSocket message", e)
                            outgoing.send(Frame.Text("error:${e.message ?: "Unknown error"}"))
                        }
                    }
                }
            } finally {
                webSocketManager.removeSession(userName)
            }
        }

    }


}

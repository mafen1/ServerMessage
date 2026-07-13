package com.example.message

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
    webSocketManager: WebSocketManager = get()
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

            webSocketManager.addSession(userName, this)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val parts = frame.readText().split(":", limit = 5)
                        try {
                            if (parts.size >= 5 && parts[0] == "to") {
                                val toUsername = parts[1]
                                val messageType = parts[2].ifBlank { "text" }
                                val clientMessageId = parts[3]
                                val textMessage = parts[4]

                                messageRepo.addMessageToDB(userName, toUsername, textMessage, messageType)
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, messageType, clientMessageId, textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, messageType, clientMessageId, textMessage)
                            } else if (parts.size >= 4) {
                                val toUsername = parts[1]
                                val messageType = parts[2].ifBlank { "text" }
                                val textMessage = parts[3]

                                messageRepo.addMessageToDB(userName, toUsername, textMessage, messageType)
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, messageType, textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, messageType, textMessage)
                            } else if (parts.size >= 3) {
                                val toUsername = parts[1]
                                val textMessage = parts[2]

                                messageRepo.addMessageToDB(userName, toUsername, textMessage)
                                webSocketManager.sendMessageCurrentUser(toUsername, userName, "text", textMessage)
                                webSocketManager.sendMessageCurrentUser(userName, userName, "text", textMessage)
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

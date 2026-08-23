package com.example.friend

import com.auth0.jwt.JWT
import com.example.login.Login
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.ktor.ext.get

fun Application.FriendWebSocket(
    loginImpl: Login = get(),
    friendWebSocketManager: FriendWebSocketManager = get()
) {
    routing {
        webSocket("/friendMessage/{username}") {
            val userName = call.parameters["username"]
            if (userName.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Username is required"))
                return@webSocket
            }

            val token = call.request.queryParameters["token"]
                ?: call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
                ?: call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            if (token.isNullOrBlank() || !loginImpl.validateToken(token)) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unauthorized"))
                return@webSocket
            }
            // проверка что токен принадлежит тому же пользователю что в пути (фикс hijack)
            val tokenUserName = try { JWT.decode(token).getClaim("userName").asString() } catch (_: Exception) { null }
            if (tokenUserName != userName) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Username mismatch"))
                return@webSocket
            }

            friendWebSocketManager.addSession(userName, this)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val parts = frame.readText().split(":", limit = 3)

                        if (parts.size >= 3) {
                            val toUsername = parts[1]
                            val textMessage = parts[2]
                            friendWebSocketManager.sendNotification(textMessage, toUsername)
                        }
                    }
                }
            } finally {
                friendWebSocketManager.removeSessionIfSame(userName, this)
            }
        }
    }
}

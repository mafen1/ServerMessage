package com.example.friend

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

interface FriendWebSocketManager {
    fun addSession(userName: String, webSocketSession: WebSocketSession)
    fun removeSession(userName: String)
    fun removeSessionIfSame(userName: String, webSocketSession: WebSocketSession)
    suspend fun sendNotification(message: String, userName: String)
    fun session(): Map<String, WebSocketSession>
}

class FriendWebSocketManagerImpl : FriendWebSocketManager {
    private val session = ConcurrentHashMap<String, WebSocketSession>()

    override fun addSession(userName: String, webSocketSession: WebSocketSession) {
        session[userName] = webSocketSession
    }

    override fun removeSession(userName: String) {
        session.remove(userName)
    }

    override fun removeSessionIfSame(userName: String, webSocketSession: WebSocketSession) {
        if (session[userName] === webSocketSession) {
            session.remove(userName)
        }
    }

    override suspend fun sendNotification(message: String, userName: String) {
        session[userName]?.send(Frame.Text(message))
    }

    override fun session() = session.toMap()
}

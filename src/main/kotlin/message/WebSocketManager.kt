package com.example.message

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

interface WebSocketManager {
    fun addSession(userName: String, webSocketSession: WebSocketSession)
    fun removeSession(userName: String)
    suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, messageType: String, message: String)
    suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, messageType: String, clientMessageId: String, message: String)
    suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, recipientUsername: String, messageType: String, clientMessageId: String, message: String)
    fun session(): Map<String, WebSocketSession>
}

class WebSocketManagerImpl : WebSocketManager {
    private val currentSession = ConcurrentHashMap<String, WebSocketSession>()

    override fun addSession(userName: String, webSocketSession: WebSocketSession) {
        currentSession[userName] = webSocketSession
    }

    override fun removeSession(userName: String) {
        currentSession.remove(userName)
    }

    override suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, messageType: String, message: String) {
        val formattedMessage = "$senderUsername:$messageType:$message"
        currentSession[userName]?.send(formattedMessage)
    }

    override suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, messageType: String, clientMessageId: String, message: String) {
        val formattedMessage = "$senderUsername:$messageType:$clientMessageId:$message"
        currentSession[userName]?.send(formattedMessage)
    }

    override suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, recipientUsername: String, messageType: String, clientMessageId: String, message: String) {
        // новый формат с recipient для правильного chatId у отправителя (фикс ENC self echo)
        val formattedMessage = "$senderUsername:$recipientUsername:$messageType:$clientMessageId:$message"
        currentSession[userName]?.send(formattedMessage)
    }

    override fun session() = currentSession.toMap()
}

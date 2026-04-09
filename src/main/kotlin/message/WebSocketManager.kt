package com.example.message

import io.ktor.websocket.*

// todo interface
object WebSocketManager {
    private val currentSession = mutableMapOf<String, WebSocketSession>()

    fun addSession(userName: String, webSocketSession: WebSocketSession) {
        currentSession[userName] = webSocketSession
        println("Добавлена сессия для $userName. Текущие сессии: ${currentSession.keys}")
    }

    suspend fun sendMessageCurrentUser(userName: String, senderUsername: String, message: String){
        // Формат: senderUsername:message
        val formattedMessage = "$senderUsername:$message"
        currentSession[userName]?.send(formattedMessage)
        println("✅ Отправка сообщения: «$formattedMessage» ➔ $userName (от $senderUsername)")
    }

    fun session() = currentSession
}
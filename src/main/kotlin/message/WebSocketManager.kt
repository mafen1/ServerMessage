package com.example.message

import io.ktor.websocket.*

object WebSocketManager {
    private val currentSession = mutableMapOf<String, WebSocketSession>()

    fun addSession(userName: String, webSocketSession: WebSocketSession) {
        currentSession[userName] = webSocketSession
        println("Добавлена сессия для $userName. Текущие сессии: ${currentSession.keys}")
    }

    suspend fun sendMessageCurrentUser(userName: String, message: String){
        currentSession[userName]?.send(message)
        println("✅ Отправка сообщения: «$message» ➔ $userName")
    }

    fun session() = currentSession
}
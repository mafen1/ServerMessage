package com.example.friend

import io.ktor.websocket.*

object WebSocketManagerForFriend {
    private val session = mutableMapOf<String, WebSocketSession>()

    fun addSession(userName: String, webSocketSession: WebSocketSession){
        session[userName] = webSocketSession
    }

    suspend fun sendNotification(message: String, userName: String){
        session[userName]?.send(Frame.Text(message))
        println("✅ Отправка сообщения: «$message» ➔ $userName")
    }
    fun session() = session
}
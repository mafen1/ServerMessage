package com.example.message

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.time.Duration.Companion.seconds


fun Application.configureSockets() {
    install(WebSockets) {

        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false

    }



    routing {
        val sessions = Collections.synchronizedList<WebSocketServerSession>(ArrayList())


        webSocket("/chat/{username}") {
            val userName = call.parameters["username"]
            println("ПОЛУЧЕННЫЙ USERNAME ИЗ URL: $userName")
            val session = WebSocketManager.session()

            if (userName != null) {
                WebSocketManager.addSession(userName, this)
                println(session)
                for (frame in session[userName]?.incoming!!) {

                    if (frame is Frame.Text) {
                        val message = frame.readText()

                        val parts = message.split(":", limit = 3)
                        println(parts.toString())
                        if (parts.size >= 3) {
                            val toUsername = parts[1]
                            val textMessage = parts[2]
                            println("ПОЛУЧЕНО СООБЩЕНИЕ ОТ $userName ДЛЯ $toUsername: $textMessage")
                            WebSocketManager.sendMessageCurrentUser(toUsername, textMessage)

                        }
                    }
                }
            }

        }

    }


}


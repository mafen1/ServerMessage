package com.example.friend

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Application.FriendWebSocket(){


//    install(WebSockets) {
//
//        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//        pingPeriod = 15.seconds
//        timeout = 15.seconds
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
//
//    }

    routing {


        webSocket("/friendMessage/{username}") {
            val userName = call.parameters["username"]
            val session = WebSocketManagerForFriend.session()

            if (userName != null){
                WebSocketManagerForFriend.addSession(userName, this)
                println(session)
                val userSession = session[userName]
                if (userSession != null) {
                    for (frame in userSession.incoming) {
                        if (frame is Frame.Text) {

                            val message = frame.readText()
                            val parts = message.split(":", limit = 3)

                            if (parts.size >= 3) {
                                val toUsername = parts[1]
                                val textMessage = parts[2]
                                println("ПОЛУЧЕНО СООБЩЕНИЕ ОТ $userName ДЛЯ $toUsername: $textMessage")
                                WebSocketManagerForFriend.sendNotification(textMessage, toUsername)
                            }
                        }
                    }
                }
            }
        }


    }
}
package com.example.data.message

import com.example.chanel
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.ConcurrentHashMap


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

        val activeSessions = ConcurrentHashMap<String, WebSocketSession>()


        webSocket("/friendRequest") {

            var friendRequest = chanel.tryReceive().getOrNull()

            if(friendRequest != null){

            activeSessions[friendRequest!!.receiverUserName] = this


            launch(Dispatchers.IO) {
                try {
                    for (event in chanel) {
                        val session = activeSessions[event.receiverUserName]
                        println("jaghs")
                        if (session != null) {
                            send("Новая заявка в друзья")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                }
            }else{
                friendRequest = chanel.receive()
            }
        }

    }


}


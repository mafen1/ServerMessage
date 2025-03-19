package com.example.data

import com.example.data.repository.MessageRepoImpl
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
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

        webSocket("/allMessages") { // websocketSession
            for (message in MessageRepoImpl().allMessage()) {
                sendSerialized(message)
                delay(500)
            }
            close(CloseReason(CloseReason.Codes.NORMAL, "All Message"))
        }
        webSocket("/chat") {
            println("Клиент подключился: ${this}")

//            val othersMessages = incoming.receive() as Frame.Text
//            println(othersMessages.readText().toString())
//
//            val a = outgoing.send(othersMessages)

            try {
                sessions += this
                for (frame in incoming) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val message = frame.readText()
                            println("Получено от клиента: $message") // Добавьте лог

                            // Отправляем сообщение всем, кроме отправителя
                            sessions.forEach { client ->
                                if (client != this) client.send(message)
                            }
                        }
                    }
                }
            } finally {
                sessions -= this
                println("sdkaj")
            }


        }
    }
}

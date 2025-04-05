package com.example.friend

import com.example.chanel
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


fun Application.FriendRouting() {
    routing {


        post("/requestFriend") {
            try {
                val friendRequest = call.receive<FriendRequest>()
                // сохраняем запрос в друзья в бд
                FriendImpl().addRequestFriend(friendRequest)

//
                launch(Dispatchers.IO) {

                    val result = chanel.trySend(friendRequest)
                    if (result.isFailure) {
                        println("Ошибка при отправке в канал: ${result.exceptionOrNull()}")
                    } else {
                        println("Сообщение отправлено успешно")
                    }

                    println(friendRequest.toString())
                }
                call.respondText("Все данные отправлены")

            } catch (e: Exception) {
                call.respond(e.message.toString())
            }
        }
    }
}

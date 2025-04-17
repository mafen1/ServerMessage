package com.example.friend

import com.example.friend.model.FriendRequest
import com.example.friend.repository.FriendImpl
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.FriendRouting() {

    routing {


        post("/requestFriend") {
            try {
                val friendRequest = call.receive<FriendRequest>()

                FriendImpl().addRequestFriend(friendRequest)
                println("пользователь ${friendRequest.senderUserName} добавил в друзья  ")


            } catch (e: Exception) {
                call.respond(e.message.toString())
            }
        }
    }
}

package com.example.friend

import com.example.friend.model.AcceptFriendRequest
import com.example.friend.model.FriendRequest
import com.example.friend.model.FriendResponse
import com.example.friend.repository.FriendImpl
import com.example.user.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun Application.FriendRouting() {

    val friendImpl = FriendImpl()

    routing {

        post("/requestFriend") {
            try {
                val friendRequest = call.receive<FriendRequest>()
                println("=== FRIEND REQUEST RECEIVED ===")
                println("senderUserName: '${friendRequest.senderUserName}'")
                println("receiverUserName: '${friendRequest.receiverUserName}'")
                println("status: '${friendRequest.status}'")
                println("================================")

                if (friendRequest.senderUserName.isBlank() || friendRequest.receiverUserName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Пустой username"))
                    return@post
                }

                if (friendRequest.senderUserName == friendRequest.receiverUserName) {
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Нельзя добавить себя в друзья"))
                    return@post
                }

                if (friendImpl.isAlreadyFriends(friendRequest.senderUserName, friendRequest.receiverUserName)) {
                    call.respond(HttpStatusCode.OK, FriendResponse(message = "Вы уже друзья"))
                    return@post
                }

                if (friendImpl.hasPendingRequest(friendRequest.senderUserName, friendRequest.receiverUserName)) {
                    call.respond(HttpStatusCode.OK, FriendResponse(message = "Заявка уже отправлена"))
                    return@post
                }

                friendImpl.addRequestFriend(friendRequest)
                println("Пользователь ${friendRequest.senderUserName} отправил заявку в друзья пользователю ${friendRequest.receiverUserName}")

                call.respond(HttpStatusCode.OK, FriendResponse(message = "Заявка в друзья отправлена"))

            } catch (e: Exception) {
                println("ERROR: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
            }
        }

        post("/acceptFriend") {
            try {
                val request = call.receive<AcceptFriendRequest>()

                if (friendImpl.acceptFriend(request.senderUserName, request.receiverUserName)) {
                    val friends = friendImpl.getFriends(request.receiverUserName)
                    call.respond(HttpStatusCode.OK, FriendResponse(
                        message = "Заявка принята",
                        friends = friends
                    ))
                    println("Пользователь ${request.receiverUserName} принял заявку от ${request.senderUserName}")
                } else {
                    call.respond(HttpStatusCode.NotFound, FriendResponse(message = "Заявка не найдена"))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
            }
        }

        post("/rejectFriend") {
            try {
                val request = call.receive<AcceptFriendRequest>()

                if (friendImpl.rejectFriend(request.senderUserName, request.receiverUserName)) {
                    call.respond(HttpStatusCode.OK, FriendResponse(message = "Заявка отклонена"))
                    println("Пользователь ${request.receiverUserName} отклонил заявку от ${request.senderUserName}")
                } else {
                    call.respond(HttpStatusCode.NotFound, FriendResponse(message = "Заявка не найдена"))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
            }
        }

        get("/friends/{username}") {
            try {
                val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Username не указан"))
                println("=== GET /friends/{username} ===")
                println("Request username: '$username'")
                
                val friends = friendImpl.getFriends(username)
                println("Response friends: $friends")
                println("================================")
                
                call.respond(HttpStatusCode.OK, FriendResponse(message = "OK", friends = friends))
            } catch (e: Exception) {
                println("ERROR in /friends/{username}: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
            }
        }

        get("/friendRequests/{username}") {
            try {
                val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Username не указан"))
                val requests = friendImpl.getFriendRequests(username)
                call.respond(HttpStatusCode.OK, FriendResponse(message = "OK", requests = requests))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
            }
        }
    }
}

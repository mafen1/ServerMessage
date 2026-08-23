package com.example.friend

import com.example.friend.model.AcceptFriendRequest
import com.example.friend.model.FriendRequest
import com.example.friend.model.FriendResponse
import com.example.friend.repository.Friend
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get


fun Application.FriendRouting(
    friendImpl: Friend = get(),
    friendWebSocketManager: FriendWebSocketManager = get()
) {

    routing {
        authenticate("jwt") {
            post("/requestFriend") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    if (tokenUser.isNullOrBlank()) {
                        call.respond(HttpStatusCode.Unauthorized, FriendResponse(message = "Unauthorized"))
                        return@post
                    }
                    val friendRequest = call.receive<FriendRequest>()
                    if (friendRequest.senderUserName != tokenUser) {
                        call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Sender mismatch"))
                        return@post
                    }

                    if (friendRequest.senderUserName.isBlank() || friendRequest.receiverUserName.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Пустой username"))
                        return@post
                    }

                    if (friendRequest.senderUserName == friendRequest.receiverUserName) {
                        call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Нельзя добавить себя в друзья"))
                        return@post
                    }

                    if (friendImpl.isAlreadyFriends(friendRequest.senderUserName, friendRequest.receiverUserName)) {
                        call.respond(HttpStatusCode.Conflict, FriendResponse(message = "Вы уже друзья"))
                        return@post
                    }

                    if (friendImpl.hasPendingRequest(friendRequest.senderUserName, friendRequest.receiverUserName)) {
                        call.respond(HttpStatusCode.Conflict, FriendResponse(message = "Заявка уже отправлена"))
                        return@post
                    }

                    friendImpl.addRequestFriend(friendRequest)

                    try {
                        friendWebSocketManager.sendNotification(
                            "Заявка от ${friendRequest.senderUserName}",
                            friendRequest.receiverUserName
                        )
                    } catch (e: Exception) {
                        call.application.environment.log.warn("Failed to send WS notification: ${e.message}")
                    }

                    call.respond(HttpStatusCode.OK, FriendResponse(message = "Заявка в друзья отправлена"))

                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to create friend request", e)
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
                }
            }

            post("/acceptFriend") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    if (tokenUser.isNullOrBlank()) {
                        call.respond(HttpStatusCode.Unauthorized, FriendResponse(message = "Unauthorized"))
                        return@post
                    }
                    val request = call.receive<AcceptFriendRequest>()
                    if (request.receiverUserName != tokenUser) {
                        call.respond(HttpStatusCode.Forbidden, FriendResponse(message = "Receiver mismatch"))
                        return@post
                    }

                    if (friendImpl.acceptFriend(request.senderUserName, request.receiverUserName)) {
                        val friends = friendImpl.getFriends(request.receiverUserName)
                        call.respond(HttpStatusCode.OK, FriendResponse(
                            message = "Заявка принята",
                            friends = friends
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, FriendResponse(message = "Заявка не найдена"))
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
                }
            }

            post("/rejectFriend") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    if (tokenUser.isNullOrBlank()) {
                        call.respond(HttpStatusCode.Unauthorized, FriendResponse(message = "Unauthorized"))
                        return@post
                    }
                    val request = call.receive<AcceptFriendRequest>()
                    if (request.receiverUserName != tokenUser) {
                        call.respond(HttpStatusCode.Forbidden, FriendResponse(message = "Receiver mismatch"))
                        return@post
                    }

                    if (friendImpl.rejectFriend(request.senderUserName, request.receiverUserName)) {
                        call.respond(HttpStatusCode.OK, FriendResponse(message = "Заявка отклонена"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, FriendResponse(message = "Заявка не найдена"))
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
                }
            }

            get("/friends/{username}") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Username не указан"))
                    if (tokenUser != username) {
                        call.respond(HttpStatusCode.Forbidden, FriendResponse(message = "Forbidden"))
                        return@get
                    }
                    val friends = friendImpl.getFriends(username)
                    call.respond(HttpStatusCode.OK, FriendResponse(message = "OK", friends = friends))
                } catch (e: Exception) {
                    call.application.environment.log.error("Failed to load friends", e)
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
                }
            }

            get("/friendRequests/{username}") {
                try {
                    val tokenUser = call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()
                    val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Username не указан"))
                    if (tokenUser != username) {
                        call.respond(HttpStatusCode.Forbidden, FriendResponse(message = "Forbidden"))
                        return@get
                    }
                    val requests = friendImpl.getFriendRequests(username)
                    call.respond(HttpStatusCode.OK, FriendResponse(message = "OK", requests = requests))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, FriendResponse(message = "Ошибка: ${e.message}"))
                }
            }
        }
    }
}

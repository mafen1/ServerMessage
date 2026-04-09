package com.example.friend.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(
    val id: Int = 0,
    val senderUserName: String,
    val receiverUserName: String,
    val status: String = "pending"
)

@Serializable
data class AcceptFriendRequest(
    val senderUserName: String,
    val receiverUserName: String
)

@Serializable
data class FriendResponse(
    val message: String,
    val friends: List<String>? = null,
    val requests: List<FriendRequest>? = null
)

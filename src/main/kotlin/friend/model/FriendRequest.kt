package com.example.friend.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(
    val id: Int,
    val senderUserName: String,
    val receiverUserName: String,
    val status: String,
)

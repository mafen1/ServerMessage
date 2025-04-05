package com.example.friend

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.math.BigInteger

@Serializable
data class FriendRequest(
    val id: Int,
    val senderUserName: String,
    val receiverUserName: String,
    val status: String,
): Event()

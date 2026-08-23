package com.example.message.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val name: String,
    val recipientUsername: String = "",
    val message: String,
    val messageType: String = "text",
    val chatId: String = "",
    val clientMessageId: String = ""
)

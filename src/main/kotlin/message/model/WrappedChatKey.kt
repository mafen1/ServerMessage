package com.example.message.model

data class WrappedKeyEntry(
    val recipientUsername: String,
    val wrappedKey: String
)

data class WrappedChatKey(
    val chatId: String,
    val recipientUsername: String,
    val wrappedKey: String,
    val version: Int
)

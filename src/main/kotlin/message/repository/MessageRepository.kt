package com.example.message.repository

import com.example.message.model.Message

interface MessageRepository {
    fun addMessageToDB(senderUsername: String, recipientUsername: String, message: String, messageType: String = "text")
    fun allMessage(): List<Message>
    fun getMessagesBetweenUsers(user1: String, user2: String): List<Message>
    fun saveWrappedChatKey(chatId: String, recipientUsername: String, wrappedKey: String)
    fun getWrappedChatKey(chatId: String, recipientUsername: String): String?
}

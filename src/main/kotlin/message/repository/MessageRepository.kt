package com.example.message.repository

import com.example.message.model.Message

interface MessageRepository {
    fun addMessageToDB(id: Int, senderUsername: String, recipientUsername: String, message: String)
    fun allMessage(): List<Message>
    fun getMessagesBetweenUsers(user1: String, user2: String): List<Message>
}
package com.example.message.repository

import com.example.message.model.Message
import com.example.message.table.MessageTable

interface MessageRepository {
    fun addMessageToDB(id: Int, name: String, message: String)
    fun allMessage(): List<Message>
//    fun findAccountIdSession(): Int
}
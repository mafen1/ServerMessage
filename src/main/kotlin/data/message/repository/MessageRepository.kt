package com.example.data.message.repository

import com.example.data.message.model.Message
import com.example.data.message.table.MessageTable

interface MessageRepository {
    fun addMessageToDB(id: Int, name: String, message: String)
    fun allMessage(): List<Message>
//    fun findAccountIdSession(): Int
}
package com.example.data.repository

import com.example.data.model.Message
import com.example.data.table.MessageTable

interface MessageRepository {
    fun addMessageToDB(id: Int, name: String, message: String)
    fun allMessage(): List<Message>
//    fun findAccountIdSession(): Int
}
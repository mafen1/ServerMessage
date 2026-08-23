package com.example.message.table

import org.jetbrains.exposed.sql.Table

object MessageTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).index()
    val recipientUsername = varchar("recipient_username", 50).default("").index()
    val message = text("message")
    val messageType = varchar("message_type", 20).default("text")
    val chatId = varchar("chat_id", 255).default("").index()
    val clientMessageId = varchar("client_message_id", 100).default("").index()

    override val primaryKey = PrimaryKey(id)
}

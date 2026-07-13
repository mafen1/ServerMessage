package com.example.message.table

import org.jetbrains.exposed.sql.Table

object ChatKeyTable : Table() {
    val id = integer("id").autoIncrement()
    val chatId = varchar("chat_id", 255).index()
    val recipientUsername = varchar("recipient_username", 100).index()
    val wrappedKey = text("wrapped_key")

    override val primaryKey = PrimaryKey(id)
}

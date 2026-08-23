package com.example.message.table

import org.jetbrains.exposed.sql.Table

object ChatKeyTable : Table() {
    val id = integer("id").autoIncrement()
    val chatId = varchar("chat_id", 255).index()
    val recipientUsername = varchar("recipient_username", 100).index()
    val wrappedKey = text("wrapped_key")
    // эпоха ключа чата: инкрементится сервером при каждой публикации
    val keyVersion = integer("key_version").default(0)

    override val primaryKey = PrimaryKey(id)
}

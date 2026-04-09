package com.example.message.table

import org.jetbrains.exposed.sql.Table

object MessageTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val recipientUsername = varchar("recipient_username", 50).default("")
    val message = text("message")
}
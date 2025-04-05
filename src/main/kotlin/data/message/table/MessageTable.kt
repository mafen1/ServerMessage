package com.example.data.message.table

import org.jetbrains.exposed.sql.Table

object MessageTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val message = text("message")
//    val idSession = integer("idSession")
}
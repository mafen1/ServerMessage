package com.example.news.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object NewsTable: Table(){

    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 255)
    val data = binary("data")
    val text: Column<String> = varchar("text", 255)
}
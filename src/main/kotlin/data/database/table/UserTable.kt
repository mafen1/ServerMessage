package com.example.data.database.table
// Импортируем функцию array

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType
import java.sql.Types.VARCHAR
import org.jetbrains.exposed.sql.functions.array.ArrayGet

object UserTable: Table() {

    override val tableName = "user"

    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val username = varchar("username", 100)
    val friend = array("listUserName", columnType = VarCharColumnType())
    val token = varchar("token", 10000)
}
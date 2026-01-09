package com.example.data.database.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType

object UserTable: Table() {

    override val tableName = "user"

    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val username = varchar("username", 100)
    val listUserName = array("listUserName", columnType = VarCharColumnType())
    val token = varchar("token", 10000)
    val password: Column<String?> = varchar("password", 100) as Column<String?>
}
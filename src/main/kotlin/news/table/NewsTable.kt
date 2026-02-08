package com.example.news.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.VarCharColumnType

object NewsTable: Table(){

    val id = integer("id").autoIncrement()
    val userNameAuthor: Column<String> = varchar("userNameAuthor", 255)
    val nameAuthor: Column<String> = varchar("nameAuthor", 255)
    val date: Column<String> = varchar("date", 255)
    val countLike: Column<Int> = integer("countLikes")
    val countComment: Column<Int> = integer("countComment")
    val avatarString: Column<String?> = varchar("avatarString", 255) as Column<String?>
    val description: Column<String> = varchar("description", 255)
    val comments = array("Comments", columnType = VarCharColumnType())
    var imageNews: Column<String> = varchar("imagenews", 255)

    override val primaryKey = PrimaryKey(id)
}
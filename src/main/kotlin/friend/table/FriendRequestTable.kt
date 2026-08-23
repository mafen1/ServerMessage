package com.example.friend.table

import org.jetbrains.exposed.sql.Table

object FriendRequestTable : Table() {

    override val tableName = "friend_request"

    val id = integer("id").autoIncrement()
    val senderUserName = varchar("sender_user_name", 100).index()
    val receiver = varchar("receiver_id", 100).index()
    val status = varchar("status", 100)

    override val primaryKey = PrimaryKey(id)
}

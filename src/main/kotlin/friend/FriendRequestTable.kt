package com.example.friend

import com.example.data.message.table.MessageTable.autoIncrement
import org.jetbrains.exposed.sql.Table

object FriendRequestTable : Table() {

    override val tableName = "friend_request"

    val id = integer("id").autoIncrement()
    val senderUserName = varchar("sender_user_name",500)
    val receiver = varchar("receiver_id",500)
    val status = varchar("status",100)
}
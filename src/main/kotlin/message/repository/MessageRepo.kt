package com.example.message.repository

import com.example.message.model.Message
import com.example.message.table.MessageTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MessageRepoImpl() : MessageRepository {

    override fun addMessageToDB(id: Int, senderUsername: String, recipientUsername: String, message: String) {
        transaction {
            MessageTable.insert {
                it[MessageTable.id] = id
                it[MessageTable.name] = senderUsername
                it[MessageTable.recipientUsername] = recipientUsername
                it[MessageTable.message] = message
            }
        }
    }

    override fun allMessage(): List<Message> = transaction {
        MessageTable.selectAll().map { row ->
            Message(
                id = row[MessageTable.id],
                name = row[MessageTable.name],
                recipientUsername = row[MessageTable.recipientUsername],
                message = row[MessageTable.message]
            )
        }
    }

    override fun getMessagesBetweenUsers(user1: String, user2: String): List<Message> = transaction {
        MessageTable.selectAll().where {
            ((MessageTable.name eq user1) and (MessageTable.recipientUsername eq user2)) or
            ((MessageTable.name eq user2) and (MessageTable.recipientUsername eq user1))
        }.map { row ->
            Message(
                id = row[MessageTable.id],
                name = row[MessageTable.name],
                recipientUsername = row[MessageTable.recipientUsername],
                message = row[MessageTable.message]
            )
        }
    }
}


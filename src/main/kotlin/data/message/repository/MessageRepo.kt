package com.example.data.message.repository

import com.example.data.message.model.Message
import com.example.data.message.table.MessageTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Random

class MessageRepoImpl() : MessageRepository {


    override fun addMessageToDB(id: Int, name: String, message: String) {
        transaction {
            MessageTable.insert {
                it[MessageTable.id] = id
                it[MessageTable.name] = name
                it[MessageTable.message] = message
//                it[MessageTable.idSession] = kotlin.random.Random.nextInt()
            }
        }
    }

    override fun allMessage(): List<Message> = transaction {
        // Выборка всех записей из таблицы Messages
        MessageTable.selectAll().map { row ->
            Message(
                id = row[MessageTable.id],
                name = row[MessageTable.name],
                message = row[MessageTable.message]
            )
        }
    }



}


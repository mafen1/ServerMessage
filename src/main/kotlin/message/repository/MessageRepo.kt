package com.example.message.repository

import com.example.message.model.Message
import com.example.message.model.WrappedChatKey
import com.example.message.model.WrappedKeyEntry
import com.example.message.table.ChatKeyTable
import com.example.message.table.MessageTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class MessageRepoImpl : MessageRepository {

    override fun addMessageToDB(senderUsername: String, recipientUsername: String, message: String, messageType: String, chatId: String, clientMessageId: String) {
        transaction {
            val computedChatId = if (chatId.isNotBlank()) chatId else listOf(senderUsername, recipientUsername).sorted().joinToString("__")
            MessageTable.insert {
                it[MessageTable.name] = senderUsername
                it[MessageTable.recipientUsername] = recipientUsername
                it[MessageTable.message] = message
                it[MessageTable.messageType] = messageType
                it[MessageTable.chatId] = computedChatId
                it[MessageTable.clientMessageId] = clientMessageId
            }
        }
    }

    override fun allMessage(): List<Message> = transaction {
        MessageTable.selectAll().map { row ->
            Message(
                id = row[MessageTable.id],
                name = row[MessageTable.name],
                recipientUsername = row[MessageTable.recipientUsername],
                message = row[MessageTable.message],
                messageType = row[MessageTable.messageType],
                chatId = try { row[MessageTable.chatId] } catch (_: Exception) { "" },
                clientMessageId = try { row[MessageTable.clientMessageId] } catch (_: Exception) { "" }
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
                message = row[MessageTable.message],
                messageType = row[MessageTable.messageType],
                chatId = try { row[MessageTable.chatId] } catch (_: Exception) { "" },
                clientMessageId = try { row[MessageTable.clientMessageId] } catch (_: Exception) { "" }
            )
        }
    }

    override fun publishWrappedChatKeys(chatId: String, entries: List<WrappedKeyEntry>): Int = transaction {
        val currentMax = ChatKeyTable.selectAll()
            .where { ChatKeyTable.chatId eq chatId }
            .toList()
            .maxOfOrNull { it[ChatKeyTable.keyVersion] } ?: 0
        // новая эпоха на весь чат — обе обёртки публикуются согласованно
        val newVersion = currentMax + 1

        for (entry in entries) {
            ChatKeyTable.deleteWhere {
                (ChatKeyTable.chatId eq chatId) and (ChatKeyTable.recipientUsername eq entry.recipientUsername)
            }
            ChatKeyTable.insert {
                it[ChatKeyTable.chatId] = chatId
                it[ChatKeyTable.recipientUsername] = entry.recipientUsername
                it[ChatKeyTable.wrappedKey] = entry.wrappedKey
                it[ChatKeyTable.keyVersion] = newVersion
            }
        }
        newVersion
    }

    override fun getWrappedChatKey(chatId: String, recipientUsername: String): WrappedChatKey? = transaction {
        ChatKeyTable.selectAll()
            .where {
                (ChatKeyTable.chatId eq chatId) and (ChatKeyTable.recipientUsername eq recipientUsername)
            }
            .orderBy(ChatKeyTable.keyVersion to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.let { row ->
                WrappedChatKey(
                    chatId = row[ChatKeyTable.chatId],
                    recipientUsername = row[ChatKeyTable.recipientUsername],
                    wrappedKey = row[ChatKeyTable.wrappedKey],
                    version = row[ChatKeyTable.keyVersion]
                )
            }
    }
}

package com.example.message.repository

import com.example.message.model.Message
import com.example.message.model.WrappedChatKey
import com.example.message.model.WrappedKeyEntry

interface MessageRepository {
    fun addMessageToDB(senderUsername: String, recipientUsername: String, message: String, messageType: String = "text", chatId: String = "", clientMessageId: String = "")
    fun allMessage(): List<Message>
    fun getMessagesBetweenUsers(user1: String, user2: String): List<Message>

    /**
     * Атомарная публикация обёрток чат-ключа: одной транзакцией апсертит все записи
     * и инкрементит общую эпоху ключа чата. Возвращает назначенную версию.
     */
    fun publishWrappedChatKeys(chatId: String, entries: List<WrappedKeyEntry>): Int

    /** Актуальная (с максимальной эпохой) обёртка ключа для получателя. */
    fun getWrappedChatKey(chatId: String, recipientUsername: String): WrappedChatKey?
}

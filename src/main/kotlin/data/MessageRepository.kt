package com.example.data

import com.zaxxer.hikari.HikariDataSource

interface MessageRepository {
    fun addMessageToDB(dataSource: HikariDataSource, data: Message)
}
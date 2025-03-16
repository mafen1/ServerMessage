package com.example.data

import com.zaxxer.hikari.HikariDataSource
import java.sql.PreparedStatement

class MessageRepoImpl() : MessageRepository {


    override fun addMessageToDB(dataSource: HikariDataSource, data: Message) {
        dataSource.connection.use { connection ->
            val sql = "INSERT INTO message (id, name, message) VALUES (? ,?, ?)"
            val preparedStatement: PreparedStatement = connection.prepareStatement(sql)
            preparedStatement.setInt(1, data.id)
            preparedStatement.setString(2, data.name)
            preparedStatement.setString(3, data.message)
            preparedStatement.executeUpdate()
        }
    }


}
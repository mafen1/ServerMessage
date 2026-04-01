package com.example.data.database

import com.example.data.database.table.UserTable
import com.example.friend.table.FriendRequestTable
import com.example.message.table.MessageTable
import com.example.news.table.NewsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {

    private lateinit var dbUrl: String
    private lateinit var dbUser: String
    private lateinit var dbPassword: String

    fun initConfig(config: ApplicationConfig) {
        println("=== ЗАГРУЖЕННЫЕ НАСТРОЙКИ ===")

        val envUrl = System.getenv("DB_POSTGRES_URL")
        val envUser = System.getenv("DB_POSTGRES_USER")
        val envPassword = ("DB_POSTGRES_PASSWORD")


        // Определяем финальные значения
        dbUrl = config.propertyOrNull("database.url")?.getString()
            ?: envUrl
                    ?: "jdbc:postgresql://localhost:5432/server_message"

        dbUser = config.propertyOrNull("database.user")?.getString()
            ?: envUser
                    ?: System.getProperty("user.name")

        dbPassword = config.propertyOrNull("database.password")?.getString()
            ?: envPassword
                    ?: ""

        println("\nИспользуемые параметры подключения:")
        println("DB URL: $dbUrl")
        println("DB USER: $dbUser")
        println("DB PASSWORD: ${if (dbPassword.isNotEmpty()) "****" else "пустой"}")
        println("=============================")
    }

    fun Application.initializationDatabase() {
        initConfig(environment.config)
        Database.connect(getHikariDatasource())

        transaction {
            SchemaUtils.create(
                MessageTable,
                UserTable,
                FriendRequestTable,
                NewsTable
            )
        }
    }


    private fun getHikariDatasource(): HikariDataSource {
        println("DB URL: $dbUrl")
        println("DB USER: $dbUser")
        println("DB PASSWORD: ${if (dbPassword.isNotEmpty()) "****" else "пустой"}")

        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 3
            isAutoCommit = true
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            validate()
        }

        return HikariDataSource(config)
    }

}



package com.example.data.database

import com.example.data.database.table.UserTable
import com.example.friend.table.FriendRequestTable
import com.example.message.table.MessageTable
import com.example.news.table.NewsTable
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect


object DatabaseFactory {

    private lateinit var dbUrl: String
    private lateinit var dbUser: String
    private lateinit var dbPassword: String

    fun initConfig(config: ApplicationConfig) {
        println("=== ЗАГРУЖЕННЫЕ НАСТРОЙКИ ===")

        // Проверяем переменные окружения
        val envUrl = System.getenv("DB_POSTGRES_URL")
        val envUser = System.getenv("DB_POSTGRES_USER")
        val envPassword = System.getenv("DB_POSTGRES_PASSWORD")

        println("Переменные окружения:")
        println("DB_POSTGRES_URL: ${envUrl ?: "не установлено"}")
        println("DB_POSTGRES_USER: ${envUser ?: "не установлено"}")
        println("DB_POSTGRES_PASSWORD: ${envPassword?.let { "****" } ?: "не установлено"}")

        // Проверяем конфигурацию
        println("\nКонфигурация из application.conf:")
        println("database.url: ${config.propertyOrNull("database.url")?.getString() ?: "не указано"}")
        println("database.user: ${config.propertyOrNull("database.user")?.getString() ?: "не указано"}")
        println("database.password: ${config.propertyOrNull("database.password")?.getString()?.let { "****" } ?: "не указано"}")

        // Определяем финальные значения
        dbUrl = config.propertyOrNull("database.url")?.getString()
            ?: envUrl
                    ?: "jdbc:postgresql://localhost:5432/server_message"

        dbUser = config.propertyOrNull("database.user")?.getString()
            ?: envUser
                    ?: System.getProperty("user.name") // Используем имя текущего пользователя macOS

        dbPassword = config.propertyOrNull("database.password")?.getString()
            ?: envPassword
                    ?: "" // Пустой пароль по умолчанию для macOS

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
            isAutoCommit = true // Установите true вместо false
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            connectionTestQuery = "SELECT 1"
            validate()
        }

        return HikariDataSource(config)
    }

}



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

        println("Загруженные настройки:")
        println("database.url = ${config.propertyOrNull("database.url")?.getString()}")
        println("database.user = ${config.propertyOrNull("database.user")?.getString()}")

        // изменить
        dbUrl = config.propertyOrNull("database.url")?.getString()
            ?: System.getenv("DB_POSTGRES_URL")
                    ?: "jdbc:postgresql://localhost:5432/postgres"
                    ?: throw IllegalStateException("Не задан database.url в application.conf или DB_POSTGRES_URL в переменных окружения")

        dbUser = config.propertyOrNull("database.user")?.getString()
            ?: System.getenv("DB_POSTGRES_USER")
                    ?: "postgres"
                    ?: throw IllegalStateException("Не задан database.user или DB_POSTGRES_USER")

        dbPassword = config.propertyOrNull("database.password")?.getString()
            ?: System.getenv("DB_POSTGRES_PASSWORD")
                    ?: "postgres"
                    ?: throw IllegalStateException("skjdhf")
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

        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()

        return HikariDataSource(config)
    }


}



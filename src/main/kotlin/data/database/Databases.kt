package com.example.data.database

import com.example.data.database.table.UserTable
import com.example.friend.table.FriendRequestTable
import com.example.message.table.ChatKeyTable
import com.example.message.table.MessageTable
import com.example.news.table.NewsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {

    private lateinit var dbUrl: String
    private lateinit var dbUser: String
    private lateinit var dbPassword: String

    fun initConfig(config: ApplicationConfig) {
        fun read(key: String, env: String, fallback: String): String =
            config.propertyOrNull("ktor.database.$key")?.getString()
                ?: System.getenv(env)
                ?: System.getProperty(env)
                ?: fallback

        dbUrl = read("url", "DB_POSTGRES_URL", "jdbc:postgresql://localhost:5432/server_message")
        dbUser = read("user", "DB_POSTGRES_USER", System.getProperty("user.name"))
        dbPassword = read("password", "DB_POSTGRES_PASSWORD", "")
    }

    fun Application.initializationDatabase() {
        initConfig(environment.config)
        log.info("Connecting to database: url=$dbUrl, user=$dbUser")
        Database.connect(getHikariDatasource())

        transaction {
            if (isPostgreSql()) {
                dropLegacyTokenColumn()
                normalizeLegacyMessageIds()
            }
            SchemaUtils.createMissingTablesAndColumns(
                UserTable,
                FriendRequestTable,
                MessageTable,
                ChatKeyTable,
                NewsTable
            )
        }
    }

    private fun isPostgreSql(): Boolean = dbUrl.startsWith("jdbc:postgresql", ignoreCase = true)

    private fun dropLegacyTokenColumn() {
        TransactionManager.current().exec(
            """
            DO $$
            BEGIN
                IF EXISTS (
                    SELECT 1
                    FROM information_schema.columns
                    WHERE table_name = 'user'
                      AND column_name = 'token'
                ) THEN
                    ALTER TABLE "user" DROP COLUMN token;
                END IF;
            END $$;
            """.trimIndent()
        )
    }

    private fun normalizeLegacyMessageIds() {
        TransactionManager.current().exec(
            """
            DO $$
            DECLARE
                id_sequence TEXT;
            BEGIN
                IF to_regclass('public.message') IS NOT NULL THEN
                    IF EXISTS (
                        SELECT 1
                        FROM public.message
                        GROUP BY id
                        HAVING COUNT(*) > 1
                    ) THEN
                        WITH numbered AS (
                            SELECT ctid, row_number() OVER (ORDER BY ctid)::integer AS new_id
                            FROM public.message
                        )
                        UPDATE public.message AS m
                        SET id = numbered.new_id
                        FROM numbered
                        WHERE m.ctid = numbered.ctid;
                    END IF;

                    SELECT pg_get_serial_sequence('public.message', 'id') INTO id_sequence;
                    IF id_sequence IS NOT NULL THEN
                        PERFORM setval(
                            id_sequence,
                            GREATEST((SELECT COALESCE(MAX(id), 0) FROM public.message), 1),
                            true
                        );
                    END IF;
                END IF;
            END $$;
            """.trimIndent()
        )
    }


    private fun getHikariDatasource(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = when {
                dbUrl.startsWith("jdbc:h2", ignoreCase = true) -> "org.h2.Driver"
                dbUrl.startsWith("jdbc:postgresql", ignoreCase = true) -> "org.postgresql.Driver"
                else -> throw IllegalArgumentException("Unsupported database URL: $dbUrl")
            }
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

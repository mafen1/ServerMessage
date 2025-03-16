package com.example

import com.example.app.DatabaseFactory
import com.example.data.Message
import com.example.data.MessageRepoImpl
import com.mongodb.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/test") {

        }
        post("/message") {
            try {
                val message = call.receive<Message>()
                MessageRepoImpl().addMessageToDB(DatabaseFactory.getHikariDatasource(), message)
                call.respondText("Данные сохранены в базу данных: $message", status = HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respondText("Ошибка: ${e.message}", status = HttpStatusCode.BadRequest)
            }

        }

    }

}



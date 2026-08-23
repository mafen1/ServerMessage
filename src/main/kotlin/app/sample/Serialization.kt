package com.example.app.sample

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            // всегда включаем поля со значениями по умолчанию (например, status="pending"
            // у заявок в друзья): иначе клиенты получают неполный JSON
            encodeDefaults = true
        })
    }
}

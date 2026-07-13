package com.example

import com.example.app.sample.configureSerialization
import com.example.authentication.AuthenticationApp
import com.example.data.database.DatabaseFactory.initializationDatabase
import com.example.di.appModule
import com.example.friend.FriendRouting
import com.example.friend.FriendWebSocket
import com.example.login.JwtSettings
import com.example.login.LoginRouting
import com.example.message.MessageRouting
import com.example.message.configureSockets
import com.example.news.routingNews
import com.example.security.KeyRouting
import com.example.user.UserRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.minutes


fun main() {
    embeddedServer(CIO, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    initializationDatabase()
    configureSerialization()

    install(Koin) {
        SLF4JLogger()
        modules(appModule(environment.config))
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost() // TODO: ограничить в production
    }

    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
        }
        register(RateLimitName("auth")) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
        }
        register(RateLimitName("upload")) {
            rateLimiter(limit = 20, refillPeriod = 1.minutes)
        }
    }

    val jwtSettings = JwtSettings.from(environment.config)

    configureSockets()
    AuthenticationApp(jwtSettings)
    LoginRouting()
    FriendRouting()
    UserRouting()
    MessageRouting()
    KeyRouting()
    FriendWebSocket()
    routingNews()

    routing {
        get("/") {
            call.respondText("Server is running", ContentType.Text.Plain)
        }
    }

}

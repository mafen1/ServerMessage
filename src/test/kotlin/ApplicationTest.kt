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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class ApplicationTest {

    private val testConfig = MapApplicationConfig(
        "ktor.database.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "ktor.database.user" to "sa",
        "ktor.database.password" to "",
        "ktor.jwt.audience" to "test-audience",
        "ktor.jwt.realm" to "test-realm",
        "ktor.jwt.domain" to "http://localhost/",
        "ktor.jwt.secret" to "test-secret-key-for-jwt-signing-only"
    )

    private fun Application.testModule() {
        initializationDatabase()
        configureSerialization()

        install(Koin) {
            SLF4JLogger()
            modules(appModule(testConfig))
        }

        install(CallLogging)

        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            anyHost()
        }

        install(RateLimit) {
            global { rateLimiter(limit = 100, refillPeriod = 1.minutes) }
            register(io.ktor.server.plugins.ratelimit.RateLimitName("auth")) {
                rateLimiter(limit = 10, refillPeriod = 1.minutes)
            }
            register(io.ktor.server.plugins.ratelimit.RateLimitName("upload")) {
                rateLimiter(limit = 20, refillPeriod = 1.minutes)
            }
        }

        val jwtSettings = JwtSettings.from(testConfig)

        install(Authentication)
        AuthenticationApp(jwtSettings)
        configureSockets()
        LoginRouting()
        FriendRouting()
        UserRouting()
        MessageRouting()
        KeyRouting()
        FriendWebSocket()
        routingNews()
    }

    @Test
    fun `register and login with hashed password`() = testApplication {
        environment { config = testConfig }
        application { testModule() }

        val registerResponse = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Test User","userName":"testuser","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"userName":"testuser","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val responseText = loginResponse.bodyAsText()
        assertTrue(responseText.contains("token"))
    }

    @Test
    fun `login with wrong password returns 401`() = testApplication {
        environment { config = testConfig }
        application { testModule() }

        client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"A","userName":"u","password":"p"}""")
        }

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"userName":"u","password":"wrong"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `me endpoint requires and accepts valid token`() = testApplication {
        environment { config = testConfig }
        application { testModule() }

        val noAuthResponse = client.get("/me")
        assertEquals(HttpStatusCode.Unauthorized, noAuthResponse.status)

        val register = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"A","userName":"u2","password":"p"}""")
        }
        val token = extractToken(register.bodyAsText())

        val meResponse = client.get("/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, meResponse.status)
    }

    @Test
    fun `chat keys publish bumps epoch and fetch returns latest copy`() = testApplication {
        environment { config = testConfig }
        application { testModule() }

        val tokenA = extractToken(
            client.post("/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"A","userName":"ka","password":"p"}""")
            }.bodyAsText()
        )
        val tokenB = extractToken(
            client.post("/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"B","userName":"kb","password":"p"}""")
            }.bodyAsText()
        )

        val chatId = "ka__kb"
        val firstPublish = client.post("/chat-keys/publish") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            setBody(
                """{"chatId":"$chatId","entries":[
                    {"recipientUsername":"kb","wrappedKey":"WRAP_FOR_B"},
                    {"recipientUsername":"ka","wrappedKey":"WRAP_FOR_A"}
                ]}"""
            )
        }
        assertEquals(HttpStatusCode.OK, firstPublish.status)
        assertEquals(1, extractVersion(firstPublish.bodyAsText()))

        val fetched = client.get("/chat-keys/$chatId?recipient=kb") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.OK, fetched.status)
        val body = fetched.bodyAsText()
        assertTrue(body.contains("WRAP_FOR_B"))
        assertEquals(1, extractVersion(body))

        // повторная публикация инкрементит эпоху и перезаписывает копии
        val secondPublish = client.post("/chat-keys") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tokenB")
            setBody("""{"chatId":"$chatId","recipientUsername":"kb","wrappedKey":"ROTATED_FOR_B"}""")
        }
        assertEquals(HttpStatusCode.OK, secondPublish.status)
        assertEquals(2, extractVersion(secondPublish.bodyAsText()))

        val refetched = client.get("/chat-keys/$chatId?recipient=kb") {
            header(HttpHeaders.Authorization, "Bearer $tokenB")
        }
        assertTrue(refetched.bodyAsText().contains("ROTATED_FOR_B"))
        assertEquals(2, extractVersion(refetched.bodyAsText()))
    }

    private fun extractVersion(body: String): Int =
        Regex("\"version\"\\s*:\\s*(\\d+)").find(body)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalStateException("version not found in body: $body")

    @Test
    fun `chat keys endpoints reject foreign chats and foreign copies`() = testApplication {
        environment { config = testConfig }
        application { testModule() }

        val tokenA = extractToken(
            client.post("/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"A","userName":"fa","password":"p"}""")
            }.bodyAsText()
        )

        // chatId не соответствует участникам
        val poisoned = client.post("/chat-keys/publish") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            setBody("""{"chatId":"fb__someoneElse","entries":[{"recipientUsername":"fb","wrappedKey":"X"}]}""")
        }
        assertEquals(HttpStatusCode.Forbidden, poisoned.status)

        // чужую копию ключа читать нельзя
        client.post("/chat-keys/publish") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            setBody("""{"chatId":"fa__fb","entries":[{"recipientUsername":"fb","wrappedKey":"X"}]}""")
        }
        val foreignFetch = client.get("/chat-keys/fa__fb?recipient=fb") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.Forbidden, foreignFetch.status)

        // своя копия читается
        val ownFetch = client.get("/chat-keys/fa__fb?recipient=fa") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        assertEquals(HttpStatusCode.NotFound, ownFetch.status)
    }

    private fun extractToken(json: String): String {
        return Json.parseToJsonElement(json)
            .jsonObject["token"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("Token not found in response: $json")
    }
}

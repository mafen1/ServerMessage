package com.example.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.login.model.LoginRequest
import com.example.login.model.LoginResponse
import com.example.user.model.User
import com.example.user.repository.UserRepositoryImpl
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class LoginImpl : Login {

    override fun createJWT(user: User): LoginResponse {
        val time = Clock.System.now().plus(70.days)

        val jwtSecret = System.getenv("JWT_SECRET") 
            ?: System.getProperty("JWT_SECRET")
            ?: "x7K9mP2vL5nQ8wR3tY6uI0oA4sD7fG1hJ"

        val token = JWT.create()
            .withAudience("user-server")
            .withIssuer("http://localhost/")
            .withExpiresAt(time.toJavaInstant())
            .sign(Algorithm.HMAC256(jwtSecret))

        return LoginResponse(
            token = token,
            expiresAt = time.toLocalDateTime(TimeZone.UTC).toString(),
            user = user
        )
    }

    override fun validateToken(token: String): Boolean {
        val newSecret = System.getenv("JWT_SECRET") 
            ?: System.getProperty("JWT_SECRET")
            ?: "x7K9mP2vL5nQ8wR3tY6uI0oA4sD7fG1hJ"
        val oldSecret = "my-secret-key-12345"
        
        return try {
            // Пробуем сначала новый secret
            JWT.require(Algorithm.HMAC256(newSecret))
                .withAudience("user-server")
                .withIssuer("http://localhost/")
                .build()
                .verify(token)
            true
        } catch (e: Exception) {
            try {
                // Пробуем старый secret для backward compatibility
                JWT.require(Algorithm.HMAC256(oldSecret))
                    .withAudience("message-app-audience")
                    .withIssuer("message-app-domain")
                    .build()
                    .verify(token)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    override fun validateUser(user: User): Boolean =
        UserRepositoryImpl().findUser(user).userName.isNotEmpty()

    override fun loginAccount(loginRequest: LoginRequest): User =
        UserRepositoryImpl().findUserByUserNamePassword(loginRequest)

    override fun validateUserByUserName(userName: String): Boolean =
        UserRepositoryImpl().findUserUserName(userName).token?.isNotEmpty()
            ?: throw IllegalArgumentException("Не найден пользователь")


}
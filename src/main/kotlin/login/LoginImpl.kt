package com.example.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.login.model.LoginRequest
import com.example.login.model.LoginResponse
import com.example.user.model.User
import com.example.user.repository.UserRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class LoginImpl(
    private val userRepository: UserRepository,
    private val jwtSettings: JwtSettings
) : Login {

    override fun createJWT(user: User): LoginResponse {
        val time = Clock.System.now().plus(70.days)

        val token = JWT.create()
            .withAudience(jwtSettings.audience)
            .withIssuer(jwtSettings.issuer)
            .withClaim("userName", user.userName)
            .withExpiresAt(time.toJavaInstant())
            .sign(Algorithm.HMAC256(jwtSettings.secret))

        return LoginResponse(
            token = token,
            expiresAt = time.toLocalDateTime(TimeZone.UTC).toString(),
            user = user
        )
    }

    override fun validateToken(token: String): Boolean =
        try {
            JWT.require(Algorithm.HMAC256(jwtSettings.secret))
                .withAudience(jwtSettings.audience)
                .withIssuer(jwtSettings.issuer)
                .build()
                .verify(token)
            true
        } catch (_: Exception) {
            false
        }

    override fun validateUser(user: User): Boolean =
        runCatching { userRepository.findUser(user.userName) }.getOrNull()?.userName?.isNotEmpty() == true

    override fun loginAccount(loginRequest: LoginRequest): LoginResponse {
        val user = userRepository.findUserByUserNamePassword(loginRequest)
        return createJWT(user)
    }

    override fun validateUserByUserName(userName: String): Boolean =
        runCatching { userRepository.findUserUserName(userName) }.getOrNull()?.userName?.isNotEmpty() == true


}

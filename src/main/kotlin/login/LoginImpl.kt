package com.example.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.login.model.LoginResponse
import com.example.data.user.model.User
import com.example.data.user.repository.UserRepositoryImpl
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class LoginImpl: Login {

    override fun createJWT(user: User): LoginResponse {

        val time = Clock.System.now().plus(70.days)
        val jwtAudience = System.getenv("audience")
        val jwtRealm = System.getenv("realm")
        val jwtDomain = System.getenv("domain")
        val jwtSecret = System.getenv("secret")

        val token = JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .withExpiresAt(time.toJavaInstant())
            .sign(Algorithm.HMAC256(jwtSecret))

        return LoginResponse(
            token = token,
            expiresAt = time.toLocalDateTime(TimeZone.UTC).toString(),
            user = user
        )
    }

    override fun validateToken(token: String): Boolean =
        UserRepositoryImpl().findUserToken(token).token!!.isNotEmpty()

    override fun validateUser(user: User): Boolean =
        UserRepositoryImpl().findUser(user).userName.isNotEmpty()

}
package com.example.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.AuthenticationApp() {

    val ktorConfig = environment.config.config("ktor")
    val jwtConfig = ktorConfig.config("jwt")


    val config = environment.config

    val jwtAudience = config.propertyOrNull("jwt.audience")?.getString()
        ?: System.getenv("JWT_AUDIENCE")
        ?: "user-server"

    val jwtRealm = config.propertyOrNull("jwt.realm")?.getString()
        ?: System.getenv("JWT_REALM")
        ?: "Access to 'hello'"

    val jwtDomain = config.propertyOrNull("jwt.domain")?.getString()
        ?: System.getenv("JWT_DOMAIN")
        ?: "http://localhost/"

    val jwtSecret = config.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_SECRET")
        ?: "ajlkhf"

    authentication {
        jwt {
            realm = jwtRealm

            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
        }
    }

}


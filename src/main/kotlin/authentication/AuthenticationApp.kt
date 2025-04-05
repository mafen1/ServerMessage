package com.example.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.AuthenticationApp() {

    val ktorConfig = environment.config.config("ktor")
    val jwtConfig = ktorConfig.config("jwt")


    val jwtAudience = System.getenv("audience")
    val jwtRealm = System.getenv("realm")
    val jwtDomain = System.getenv("domain")
    val jwtSecret = System.getenv("secret")

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


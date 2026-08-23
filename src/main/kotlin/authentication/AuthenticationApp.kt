package com.example.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.login.JwtSettings
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.AuthenticationApp(jwtSettings: JwtSettings = JwtSettings.from(environment.config)) {

    authentication {
        jwt("jwt") {
            realm = jwtSettings.realm

            verifier(
                JWT.require(Algorithm.HMAC256(jwtSettings.secret))
                    .withAudience(jwtSettings.audience)
                    .withIssuer(jwtSettings.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtSettings.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

}

package com.example.login

import io.ktor.server.config.ApplicationConfig

data class JwtSettings(
    val audience: String,
    val realm: String,
    val issuer: String,
    val secret: String
) {
    companion object {
        private const val DEFAULT_AUDIENCE = "user-server"
        private const val DEFAULT_REALM = "Access to 'hello'"
        private const val DEFAULT_ISSUER = "http://localhost/"

        fun from(config: ApplicationConfig): JwtSettings {
            fun read(key: String, env: String, fallback: String): String =
                config.propertyOrNull("ktor.jwt.$key")?.getString()
                    ?: System.getenv(env)
                    ?: System.getProperty(env)
                    ?: fallback

            fun readRequired(key: String, env: String): String =
                config.propertyOrNull("ktor.jwt.$key")?.getString()
                    ?: System.getenv(env)
                    ?: System.getProperty(env)
                    ?: throw IllegalStateException(
                        "JWT secret is not configured. " +
                        "Set JWT_SECRET environment variable or ktor.jwt.secret in application.conf."
                    )

            return JwtSettings(
                audience = read("audience", "JWT_AUDIENCE", DEFAULT_AUDIENCE),
                realm = read("realm", "JWT_REALM", DEFAULT_REALM),
                issuer = read("domain", "JWT_DOMAIN", DEFAULT_ISSUER),
                secret = readRequired("secret", "JWT_SECRET")
            )
        }
    }
}

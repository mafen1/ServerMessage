package com.example.login.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    var userName: String,
    var password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: com.example.user.model.User
)

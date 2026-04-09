package com.example.login.model

import com.example.user.model.User
import kotlinx.serialization.Serializable

// todo password сделать
@Serializable
data class LoginRequest(
    var name: String,
    var userName: String,
    var password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: User
)

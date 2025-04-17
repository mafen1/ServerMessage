package com.example.login.model

import com.example.user.model.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: User
)

package com.example.login.model

import com.example.data.user.model.User
import kotlinx.serialization.Serializable


//todo login response

@Serializable
data class RegisterResponse (
    val token: String,
    val expiresAt: String,
    val user: User
)
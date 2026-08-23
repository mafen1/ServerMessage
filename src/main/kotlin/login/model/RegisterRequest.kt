package com.example.login.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val userName: String,
    val password: String
)

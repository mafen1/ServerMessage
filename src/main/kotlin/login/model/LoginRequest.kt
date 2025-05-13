package com.example.login.model

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    var userName: String,
    var password: String
)

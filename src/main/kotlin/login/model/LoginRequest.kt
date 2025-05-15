package com.example.login.model

import kotlinx.serialization.Serializable

// todo password сделать
@Serializable
data class LoginRequest(
    var name: String,
    var username: String
)

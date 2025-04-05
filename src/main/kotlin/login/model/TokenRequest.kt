package com.example.login.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    var token: String
)

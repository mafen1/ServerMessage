package com.example.data.user.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val username: String
)

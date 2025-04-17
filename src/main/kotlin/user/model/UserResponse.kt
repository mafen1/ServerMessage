package com.example.user.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val userName: String,
    val name: String
)

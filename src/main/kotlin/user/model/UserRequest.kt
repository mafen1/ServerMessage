package com.example.user.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val userName: String
)

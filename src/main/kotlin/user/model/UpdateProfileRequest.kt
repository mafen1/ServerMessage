package com.example.user.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val userName: String,
    val name: String,
    val password: String? = null
)

package com.example.user.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val userName: String,
    val friend: List<String>?,
    val token: String?
)
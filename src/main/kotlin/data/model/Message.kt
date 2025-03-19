package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val name: String,
    val message: String
)

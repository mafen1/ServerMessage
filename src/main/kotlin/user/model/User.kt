package com.example.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int,
    val name: String,
    @SerialName("username") // ← Важно! Синхронизируем имена
    val username: String,
    @SerialName("friend") // ← Важно!
    val listUserName: List<String>?,
    val token: String?,
    val password: String?
)




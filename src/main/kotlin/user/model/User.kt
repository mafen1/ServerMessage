package com.example.user.model

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int = 0,
    val name: String = "",
    val userName: String = "",
    val listUserName: List<String>? = emptyList(),
    val password: String? = ""
)




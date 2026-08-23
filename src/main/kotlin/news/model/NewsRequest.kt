package com.example.news.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsRequest(
    var userNameAuthor: String,
    var nameAuthor: String,
    var date: String,
    val countLike: Int,
    val countComment: Int,
    // значения по умолчанию: старые клиенты могут не присылать эти поля
    val avatarAuthor: String? = null,
    val description: String = "",
    val comment: List<String> = emptyList()
)

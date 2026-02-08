package com.example.news.model

import kotlinx.serialization.Serializable

// todo count comment Like
@Serializable
data class NewsRequest(
    var userNameAuthor: String,
    var nameAuthor: String,
    var date: String,
    val countLike: Int,
    val countComment: Int,
    val avatarAuthor: String?,
    val description: String,
    val comment: List<String>

)

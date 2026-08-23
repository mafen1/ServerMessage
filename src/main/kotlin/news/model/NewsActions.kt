package com.example.news.model

import kotlinx.serialization.Serializable

@Serializable
data class LikeRequest(
    val newsId: Int,
    val userName: String
)

@Serializable
data class CommentRequest(
    val newsId: Int,
    val userName: String,
    val text: String
)

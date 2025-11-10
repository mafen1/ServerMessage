package com.example.news.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsWithOutImage(
    var id: Int,
    var userName: String,
    var text: String
)

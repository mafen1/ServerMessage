package com.example.news.model

import kotlinx.serialization.Serializable

// todo переделать дату
@Serializable
data class NewsRequest(
    var id: Int,
    var userName: String,
    var data: ByteArray,
    var text: String
)

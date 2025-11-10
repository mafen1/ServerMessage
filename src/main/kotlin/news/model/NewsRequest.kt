package com.example.news.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody


@Serializable
data class NewsRequest(
    var id: Int,
    var userName: String,
    var image: String?,
    var text: String
)

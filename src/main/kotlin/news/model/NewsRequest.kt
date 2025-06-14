package com.example.news.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody


// todo переделать дату
@Serializable
data class NewsRequest(
    var id: Int,
    var userName: String,
    @Contextual
    var image: MultipartBody.Part,
    var text: String
)

package com.example.news.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class News(
    var id: Int,
    var userName: String,
    @Contextual
    var image: File,
    var text: String
)

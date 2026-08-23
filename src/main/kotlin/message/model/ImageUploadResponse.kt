package com.example.message.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val fileName: String
)

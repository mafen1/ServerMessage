package com.example.security.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyRequest(val publicKey: String)

@Serializable
data class WrappedKeyRequest(
    val chatId: String,
    val recipientUsername: String,
    val wrappedKey: String
)

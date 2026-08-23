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

@Serializable
data class PublishChatKeysRequest(
    val chatId: String,
    val entries: List<PublishKeyEntry>
)

@Serializable
data class PublishKeyEntry(
    val recipientUsername: String,
    val wrappedKey: String
)

@Serializable
data class PublishChatKeysResponse(
    val success: Boolean,
    val version: Int
)

@Serializable
data class WrappedKeyWithVersionResponse(
    val chatId: String,
    val wrappedKey: String,
    val version: Int
)

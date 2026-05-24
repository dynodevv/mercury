package com.dynodevv.mercury.domain.model

data class AiChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

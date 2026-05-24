package com.dynodevv.mercury.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val role: String,
    val content: String
)

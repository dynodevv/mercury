package com.dynodevv.mercury.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<AiMessage>,
    val stream: Boolean = true,
    @SerialName("max_tokens")
    val maxTokens: Int = 2048
)

@Serializable
data class OpenAiResponse(
    val choices: List<OpenAiChoice> = emptyList()
)

@Serializable
data class OpenAiChoice(
    val delta: OpenAiDelta? = null,
    val message: OpenAiMessage? = null
)

@Serializable
data class OpenAiDelta(
    val content: String? = null
)

@Serializable
data class OpenAiMessage(
    val content: String? = null
)

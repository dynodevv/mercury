package com.dynodevv.mercury.domain.model

data class AiProvider(
    val provider: String,
    val apiKey: String,
    val model: String,
    val customEndpoint: String? = null
)

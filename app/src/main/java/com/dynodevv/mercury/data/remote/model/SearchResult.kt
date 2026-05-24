package com.dynodevv.mercury.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val index: Int
)

package com.dynodevv.mercury.domain.model

data class TabInfo(
    val id: String,
    var url: String,
    var title: String = "",
    var favicon: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

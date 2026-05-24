package com.dynodevv.mercury.domain.model

sealed class SearchEngine(val key: String, val name: String, val searchUrl: String) {
    data object Google : SearchEngine("google", "Google", "https://www.google.com/search?q=")
    data object Bing : SearchEngine("bing", "Bing", "https://www.bing.com/search?q=")
    data object DuckDuckGo : SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=")
    data object Brave : SearchEngine("brave", "Brave", "https://search.brave.com/search?q=")
    data object Ecosia : SearchEngine("ecosia", "Ecosia", "https://www.ecosia.org/search?q=")

    companion object {
        fun fromKey(key: String): SearchEngine {
            return when (key) {
                "google" -> Google
                "bing" -> Bing
                "brave" -> Brave
                "ecosia" -> Ecosia
                else -> DuckDuckGo
            }
        }

        fun all(): List<SearchEngine> = listOf(Google, Bing, DuckDuckGo, Brave, Ecosia)
    }
}

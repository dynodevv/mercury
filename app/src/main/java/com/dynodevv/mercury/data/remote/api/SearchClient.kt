package com.dynodevv.mercury.data.remote.api

import com.dynodevv.mercury.data.remote.model.SearchResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchClient @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val DDG_LITE_URL = "https://duckduckgo.com/html/"
    }

    suspend fun searchDuckDuckGo(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get(DDG_LITE_URL) {
                headers {
                    append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                }
                parameter("q", query)
                parameter("kl", "us-en")
            }
            val html = response.bodyAsText()
            parseDuckDuckGoHtml(html)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseDuckDuckGoHtml(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val doc = Jsoup.parse(html)
        val elements = doc.select("div.result")

        elements.take(10).forEachIndexed { index, element ->
            val titleElement = element.selectFirst("a.result__a")
            val urlElement = element.selectFirst("a.result__url")
            val snippetElement = element.selectFirst("a.result__snippet")

            val title = titleElement?.text() ?: return@forEachIndexed
            val url = titleElement.attr("href")
                .takeIf { it.isNotBlank() }
                ?: urlElement?.text()
                ?: return@forEachIndexed
            val snippet = snippetElement?.text() ?: ""

            results.add(
                SearchResult(
                    title = title,
                    url = resolveUrl(url),
                    snippet = snippet,
                    index = index + 1
                )
            )
        }

        return results
    }

    private fun resolveUrl(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            else -> "https://$url"
        }
    }
}

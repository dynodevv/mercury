package com.dynodevv.mercury.service.adblock

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdBlockService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "AdBlockService"
        private const val OISD_URL = "https://raw.githubusercontent.com/sjhgvr/oisd/main/dnsmasq/dnsmasq.txt"
        private const val BLOCKLIST_FILENAME = "oisd_blocklist.txt"
    }

    private val blocklistFile: File by lazy {
        File(context.cacheDir, BLOCKLIST_FILENAME)
    }

    private var blockedDomains: Set<String> = emptySet()
    private var isLoaded = false

    suspend fun initialize() {
        if (isLoaded) return
        withContext(Dispatchers.IO) {
            if (blocklistFile.exists()) {
                loadBlocklistFromFile()
            }
            isLoaded = true
        }
    }

    suspend fun updateBlocklist(httpClient: HttpClient): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get(OISD_URL)
            val body = response.bodyAsText()
            blocklistFile.writeText(body)
            loadBlocklistFromFile()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update blocklist", e)
            false
        }
    }

    private fun loadBlocklistFromFile() {
        val lines = blocklistFile.readLines()
        val domains = mutableSetOf<String>()
        lines.forEach { line ->
            if (line.startsWith("address=/")) {
                val domain = line.substringAfter("address=/").substringBefore("/")
                if (domain.isNotBlank()) {
                    domains.add(domain)
                }
            }
        }
        blockedDomains = domains
        Log.i(TAG, "Loaded ${domains.size} blocked domains")
    }

    fun isBlocked(url: String): Boolean {
        if (!isLoaded || blockedDomains.isEmpty()) return false
        return try {
            val host = URL(url).host.removePrefix("www.")
            blockedDomains.contains(host) || blockedDomains.contains("www.$host")
        } catch (e: Exception) {
            false
        }
    }

    fun isInitialized(): Boolean = isLoaded
}

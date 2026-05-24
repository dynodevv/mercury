package com.dynodevv.mercury.data.local.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dynodevv.mercury.domain.model.AiProvider
import com.dynodevv.mercury.domain.model.SearchEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val THEME = stringPreferencesKey("theme")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val HOMEPAGE_URL = stringPreferencesKey("homepage_url")
        val AD_BLOCKER_ENABLED = booleanPreferencesKey("ad_blocker_enabled")
        val BLOCKLIST_LAST_UPDATED = longPreferencesKey("blocklist_last_updated")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val AI_CUSTOM_ENDPOINT = stringPreferencesKey("ai_custom_endpoint")
        val AI_SEARCH_ENABLED = booleanPreferencesKey("ai_search_enabled")
    }

    val theme: Flow<String> = dataStore.data.map { it[THEME] ?: "system" }
    val searchEngine: Flow<String> = dataStore.data.map { it[SEARCH_ENGINE] ?: "duckduckgo" }
    val homepageUrl: Flow<String> = dataStore.data.map { it[HOMEPAGE_URL] ?: "mercury://homepage" }
    val adBlockerEnabled: Flow<Boolean> = dataStore.data.map { it[AD_BLOCKER_ENABLED] ?: true }
    val blocklistLastUpdated: Flow<Long> = dataStore.data.map { it[BLOCKLIST_LAST_UPDATED] ?: 0L }
    val aiProvider: Flow<String> = dataStore.data.map { it[AI_PROVIDER] ?: "" }
    val aiApiKey: Flow<String> = dataStore.data.map { it[AI_API_KEY] ?: "" }
    val aiModel: Flow<String> = dataStore.data.map { it[AI_MODEL] ?: "" }
    val aiCustomEndpoint: Flow<String> = dataStore.data.map { it[AI_CUSTOM_ENDPOINT] ?: "" }
    val aiSearchEnabled: Flow<Boolean> = dataStore.data.map { it[AI_SEARCH_ENABLED] ?: true }

    suspend fun setTheme(value: String) = dataStore.edit { it[THEME] = value }
    suspend fun setSearchEngine(value: String) = dataStore.edit { it[SEARCH_ENGINE] = value }
    suspend fun setHomepageUrl(value: String) = dataStore.edit { it[HOMEPAGE_URL] = value }
    suspend fun setAdBlockerEnabled(value: Boolean) = dataStore.edit { it[AD_BLOCKER_ENABLED] = value }
    suspend fun setBlocklistLastUpdated(value: Long) = dataStore.edit { it[BLOCKLIST_LAST_UPDATED] = value }
    suspend fun setAiProvider(value: String) = dataStore.edit { it[AI_PROVIDER] = value }
    suspend fun setAiApiKey(value: String) = dataStore.edit { it[AI_API_KEY] = value }
    suspend fun setAiModel(value: String) = dataStore.edit { it[AI_MODEL] = value }
    suspend fun setAiCustomEndpoint(value: String) = dataStore.edit { it[AI_CUSTOM_ENDPOINT] = value }
    suspend fun setAiSearchEnabled(value: Boolean) = dataStore.edit { it[AI_SEARCH_ENABLED] = value }

    fun getAiProviderConfig(): Flow<AiProvider?> = dataStore.data.map { prefs ->
        val provider = prefs[AI_PROVIDER] ?: return@map null
        val apiKey = prefs[AI_API_KEY] ?: return@map null
        val model = prefs[AI_MODEL] ?: return@map null
        AiProvider(
            provider = provider,
            apiKey = apiKey,
            model = model,
            customEndpoint = prefs[AI_CUSTOM_ENDPOINT]
        )
    }

    fun getSearchEngineConfig(): Flow<SearchEngine> = dataStore.data.map { prefs ->
        val key = prefs[SEARCH_ENGINE] ?: "duckduckgo"
        SearchEngine.fromKey(key)
    }
}

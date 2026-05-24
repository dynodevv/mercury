package com.dynodevv.mercury.data.repository

import com.dynodevv.mercury.data.local.datastore.SettingsDataStore
import com.dynodevv.mercury.domain.model.AiProvider
import com.dynodevv.mercury.domain.model.SearchEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val theme: Flow<String> = settingsDataStore.theme
    val searchEngine: Flow<SearchEngine> = settingsDataStore.getSearchEngineConfig()
    val homepageUrl: Flow<String> = settingsDataStore.homepageUrl
    val adBlockerEnabled: Flow<Boolean> = settingsDataStore.adBlockerEnabled
    val blocklistLastUpdated: Flow<Long> = settingsDataStore.blocklistLastUpdated
    val aiProviderConfig: Flow<AiProvider?> = settingsDataStore.getAiProviderConfig()
    val aiSearchEnabled: Flow<Boolean> = settingsDataStore.aiSearchEnabled

    suspend fun setTheme(theme: String) = settingsDataStore.setTheme(theme)
    suspend fun setSearchEngine(engine: String) = settingsDataStore.setSearchEngine(engine)
    suspend fun setHomepageUrl(url: String) = settingsDataStore.setHomepageUrl(url)
    suspend fun setAdBlockerEnabled(enabled: Boolean) = settingsDataStore.setAdBlockerEnabled(enabled)
    suspend fun setBlocklistLastUpdated(time: Long) = settingsDataStore.setBlocklistLastUpdated(time)
    suspend fun setAiProviderConfig(provider: AiProvider) {
        settingsDataStore.setAiProvider(provider.provider)
        settingsDataStore.setAiApiKey(provider.apiKey)
        settingsDataStore.setAiModel(provider.model)
        provider.customEndpoint?.let { settingsDataStore.setAiCustomEndpoint(it) }
    }
    suspend fun setAiSearchEnabled(enabled: Boolean) = settingsDataStore.setAiSearchEnabled(enabled)
}

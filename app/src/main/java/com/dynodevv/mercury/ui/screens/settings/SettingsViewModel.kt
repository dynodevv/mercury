package com.dynodevv.mercury.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynodevv.mercury.data.repository.SettingsRepository
import com.dynodevv.mercury.domain.model.AiProvider
import com.dynodevv.mercury.domain.model.SearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<String> = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val searchEngine: StateFlow<SearchEngine> = settingsRepository.searchEngine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchEngine.DuckDuckGo)

    val adBlockerEnabled: StateFlow<Boolean> = settingsRepository.adBlockerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val aiProviderConfig: StateFlow<AiProvider?> = settingsRepository.aiProviderConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val aiSearchEnabled: StateFlow<Boolean> = settingsRepository.aiSearchEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val blocklistLastUpdated: StateFlow<Long> = settingsRepository.blocklistLastUpdated
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setTheme(theme: String) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setSearchEngine(engine: String) {
        viewModelScope.launch { settingsRepository.setSearchEngine(engine) }
    }

    fun setAdBlockerEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAdBlockerEnabled(enabled) }
    }

    fun setAiProviderConfig(config: AiProvider) {
        viewModelScope.launch { settingsRepository.setAiProviderConfig(config) }
    }

    fun setAiSearchEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAiSearchEnabled(enabled) }
    }

    fun setBlocklistLastUpdated(time: Long) {
        viewModelScope.launch { settingsRepository.setBlocklistLastUpdated(time) }
    }
}

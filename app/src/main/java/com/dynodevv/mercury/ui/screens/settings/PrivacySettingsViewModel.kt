package com.dynodevv.mercury.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynodevv.mercury.data.repository.SettingsRepository
import com.dynodevv.mercury.service.adblock.AdBlockService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val adBlockService: AdBlockService,
    private val httpClient: HttpClient
) : ViewModel() {

    val adBlockerEnabled = settingsRepository.adBlockerEnabled
    val blocklistLastUpdated = settingsRepository.blocklistLastUpdated

    fun setAdBlockerEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAdBlockerEnabled(enabled) }
    }

    fun updateBlocklist() {
        viewModelScope.launch {
            val success = adBlockService.updateBlocklist(httpClient)
            if (success) {
                settingsRepository.setBlocklistLastUpdated(System.currentTimeMillis())
            }
        }
    }
}

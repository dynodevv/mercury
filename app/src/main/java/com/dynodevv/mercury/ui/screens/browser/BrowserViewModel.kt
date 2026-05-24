package com.dynodevv.mercury.ui.screens.browser

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynodevv.mercury.data.repository.BookmarkRepository
import com.dynodevv.mercury.data.repository.HistoryRepository
import com.dynodevv.mercury.data.repository.SettingsRepository
import com.dynodevv.mercury.service.adblock.AdBlockService
import com.dynodevv.mercury.domain.model.TabInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val settingsRepository: SettingsRepository,
    val adBlockService: AdBlockService
) : ViewModel() {

    companion object {
        private const val TAG = "BrowserViewModel"
    }

    private val _currentUrl = MutableStateFlow("mercury://homepage")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pageTitle = MutableStateFlow("Mercury")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _showAiChat = MutableStateFlow(false)
    val showAiChat: StateFlow<Boolean> = _showAiChat.asStateFlow()

    private val _pageContent = MutableStateFlow("")
    val pageContent: StateFlow<String> = _pageContent.asStateFlow()

    private val _homepageUrl = MutableStateFlow("mercury://homepage")
    val homepageUrl: StateFlow<String> = _homepageUrl.asStateFlow()

    val tabs = mutableStateListOf<TabInfo>()
    var currentTabIndex by mutableStateOf(0)

    private var currentWebView: WebView? = null

    init {
        viewModelScope.launch {
            settingsRepository.homepageUrl.collect {
                _homepageUrl.value = it
            }
        }
        addNewTab()
    }

    fun setWebView(webView: WebView?) {
        currentWebView = webView
    }

    fun loadUrl(url: String) {
        if (url == "mercury://homepage") {
            _currentUrl.value = url
            return
        }
        val formattedUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("mercury://") -> url
            else -> "https://$url"
        }
        _currentUrl.value = formattedUrl
        viewModelScope.launch {
            if (tabs.isNotEmpty()) {
                tabs[currentTabIndex] = tabs[currentTabIndex].copy(url = formattedUrl)
            }
        }
    }

    fun onPageStarted(url: String) {
        _isLoading.value = true
        _currentUrl.value = url
    }

    fun onPageFinished(url: String, title: String?) {
        _isLoading.value = false
        _currentUrl.value = url
        _pageTitle.value = title ?: url
        _canGoBack.value = currentWebView?.canGoBack() ?: false
        _canGoForward.value = currentWebView?.canGoForward() ?: false

        viewModelScope.launch {
            if (!url.startsWith("mercury://") && url.isNotBlank()) {
                historyRepository.addHistoryItem(title ?: url, url)
            }
            if (tabs.isNotEmpty()) {
                tabs[currentTabIndex] = tabs[currentTabIndex].copy(
                    url = url,
                    title = title ?: url
                )
            }
            _isBookmarked.value = bookmarkRepository.isBookmarked(url)
        }

        extractPageContent()
    }

    fun onProgressChanged(progress: Int) {
        // Could expose this if needed
    }

    fun goBack() {
        currentWebView?.goBack()
    }

    fun goForward() {
        currentWebView?.goForward()
    }

    fun reload() {
        currentWebView?.reload()
    }

    fun stopLoading() {
        currentWebView?.stopLoading()
        _isLoading.value = false
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val url = _currentUrl.value
            val title = _pageTitle.value
            if (_isBookmarked.value) {
                bookmarkRepository.removeBookmark(url)
            } else {
                bookmarkRepository.addBookmark(title, url)
            }
            _isBookmarked.value = !_isBookmarked.value
        }
    }

    fun addNewTab() {
        val newTab = TabInfo(
            id = System.currentTimeMillis().toString(),
            url = "mercury://homepage"
        )
        tabs.add(newTab)
        currentTabIndex = tabs.size - 1
        _currentUrl.value = newTab.url
    }

    fun switchToTab(index: Int) {
        if (index in tabs.indices && index != currentTabIndex) {
            currentTabIndex = index
            val newTab = tabs[index]
            _currentUrl.value = newTab.url
        }
    }

    fun closeTab(index: Int) {
        if (tabs.size <= 1) {
            tabs.clear()
            addNewTab()
            return
        }
        tabs.removeAt(index)
        if (currentTabIndex >= tabs.size) {
            currentTabIndex = tabs.size - 1
        }
        _currentUrl.value = tabs[currentTabIndex].url
    }

    fun toggleAiChat() {
        _showAiChat.value = !_showAiChat.value
    }

    fun hideAiChat() {
        _showAiChat.value = false
    }

    private fun extractPageContent() {
        currentWebView?.evaluateJavascript(
            """
            (function() {
                var title = document.title || '';
                var url = window.location.href || '';
                var desc = document.querySelector('meta[name="description"]')?.content || '';
                var text = document.body.innerText || '';
                if (text.length > 12000) text = text.substring(0, 12000);
                return JSON.stringify({
                    title: title,
                    url: url,
                    description: desc,
                    text: text
                });
            })();
            """.trimIndent()
        ) { result ->
            try {
                val clean = result?.trim()?.removePrefix("\"")?.removeSuffix("\"")?.replace("\\\"", "\"")?.replace("\\\\", "\\")
                _pageContent.value = clean ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse page content", e)
                _pageContent.value = ""
            }
        }
    }
}

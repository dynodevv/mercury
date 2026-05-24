package com.dynodevv.mercury.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynodevv.mercury.data.remote.api.SearchClient
import com.dynodevv.mercury.data.remote.model.AiMessage
import com.dynodevv.mercury.data.remote.model.SearchResult
import com.dynodevv.mercury.data.repository.AiRepository
import com.dynodevv.mercury.data.repository.SettingsRepository
import com.dynodevv.mercury.domain.model.AiChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchClient: SearchClient,
    private val aiRepository: AiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    sealed class SearchState {
        data object Idle : SearchState()
        data object LoadingResults : SearchState()
        data class Results(val results: List<SearchResult>) : SearchState()
        data class AiSummary(
            val results: List<SearchResult>,
            val summary: String,
            val followUpMessages: List<AiChatMessage> = emptyList(),
            val isLoadingFollowUp: Boolean = false
        ) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
    }

    fun search(query: String) {
        _query.value = query
        viewModelScope.launch {
            _state.value = SearchState.LoadingResults
            try {
                val results = searchClient.searchDuckDuckGo(query)
                if (results.isEmpty()) {
                    _state.value = SearchState.Error("No results found.")
                    return@launch
                }
                _state.value = SearchState.Results(results)
            } catch (e: Exception) {
                _state.value = SearchState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun summarizeWithAi() {
        val currentState = _state.value as? SearchState.Results ?: return
        viewModelScope.launch {
            generateAiSummary(_query.value, currentState.results)
        }
    }

    private fun generateAiSummary(query: String, results: List<SearchResult>) {
        viewModelScope.launch {
            val context = buildString {
                appendLine("The user searched for: \"$query\"")
                appendLine("Here are the top search results from the web:")
                results.forEach { result ->
                    appendLine("[${result.index}] ${result.title}")
                    appendLine("URL: ${result.url}")
                    appendLine("Snippet: ${result.snippet}")
                    appendLine()
                }
                appendLine("Please provide a concise, accurate summary of these results. Cite sources using [1], [2], etc. Be factual and helpful.")
            }

            val messages = listOf(
                AiMessage(role = "system", content = "You are Mercury AI Search, an intelligent search assistant. Summarize web search results accurately and cite your sources."),
                AiMessage(role = "user", content = context)
            )

            val summaryBuilder = StringBuilder()
            var summary = ""
            aiRepository.streamChatCompletion(messages).collect { chunk ->
                summaryBuilder.append(chunk)
                summary = summaryBuilder.toString()
                _state.value = SearchState.AiSummary(
                    results = results,
                    summary = summary,
                    followUpMessages = emptyList(),
                    isLoadingFollowUp = false
                )
            }
        }
    }

    fun sendFollowUp(question: String) {
        val currentState = _state.value as? SearchState.AiSummary ?: return
        viewModelScope.launch {
            val updatedMessages = currentState.followUpMessages.toMutableList()
            updatedMessages.add(AiChatMessage(role = "user", content = question))

            _state.value = currentState.copy(
                followUpMessages = updatedMessages,
                isLoadingFollowUp = true
            )

            val apiMessages = mutableListOf<AiMessage>()
            apiMessages.add(AiMessage(role = "system", content = "You are Mercury AI Search. Continue the conversation based on the search results context. Cite sources when relevant."))
            apiMessages.add(AiMessage(role = "user", content = "Search query: ${query.value}"))
            apiMessages.add(AiMessage(role = "assistant", content = currentState.summary))
            updatedMessages.forEach { msg ->
                apiMessages.add(AiMessage(role = msg.role, content = msg.content))
            }

            val responseBuilder = StringBuilder()
            aiRepository.streamChatCompletion(apiMessages).collect { chunk ->
                responseBuilder.append(chunk)
                val assistantMessage = AiChatMessage(role = "assistant", content = responseBuilder.toString())
                val allMessages = updatedMessages.toMutableList()
                if (allMessages.lastOrNull()?.role == "assistant") {
                    allMessages[allMessages.size - 1] = assistantMessage
                } else {
                    allMessages.add(assistantMessage)
                }
                _state.value = currentState.copy(
                    followUpMessages = allMessages,
                    isLoadingFollowUp = false
                )
            }
        }
    }
}

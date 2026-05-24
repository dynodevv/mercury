package com.dynodevv.mercury.ui.screens.ai_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynodevv.mercury.data.remote.model.AiMessage
import com.dynodevv.mercury.data.repository.AiRepository
import com.dynodevv.mercury.domain.model.AiChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun sendMessage(userMessage: String, pageContext: String? = null) {
        viewModelScope.launch {
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(AiChatMessage(role = "user", content = userMessage))
            _messages.value = currentMessages
            _isLoading.value = true
            _error.value = null

            val apiMessages = mutableListOf<AiMessage>()

            // Add system context about the page if available
            if (!pageContext.isNullOrBlank()) {
                apiMessages.add(
                    AiMessage(
                        role = "system",
                        content = "You are Mercury, an AI assistant embedded in a web browser. The user is currently viewing a webpage. Here is the context of the page:\n\n$pageContext\n\nAnswer the user's questions based on this context when relevant. You can also use your general knowledge."
                    )
                )
            } else {
                apiMessages.add(
                    AiMessage(
                        role = "system",
                        content = "You are Mercury, an AI assistant embedded in a web browser. Help the user with their questions."
                    )
                )
            }

            // Add conversation history
            currentMessages.forEach { msg ->
                apiMessages.add(AiMessage(role = msg.role, content = msg.content))
            }

            val responseBuilder = StringBuilder()
            aiRepository.streamChatCompletion(apiMessages).collect { chunk ->
                responseBuilder.append(chunk)
                val updatedMessages = _messages.value.toMutableList()
                // If last message is assistant, update it; else add new
                if (updatedMessages.lastOrNull()?.role == "assistant") {
                    updatedMessages[updatedMessages.size - 1] = updatedMessages.last().copy(
                        content = responseBuilder.toString()
                    )
                } else {
                    updatedMessages.add(
                        AiChatMessage(role = "assistant", content = responseBuilder.toString())
                    )
                }
                _messages.value = updatedMessages
            }

            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }
}

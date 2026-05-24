package com.dynodevv.mercury.data.repository

import android.util.Log
import com.dynodevv.mercury.data.local.datastore.SettingsDataStore
import com.dynodevv.mercury.data.remote.model.AiMessage
import com.dynodevv.mercury.data.remote.model.GeminiContent
import com.dynodevv.mercury.data.remote.model.GeminiPart
import com.dynodevv.mercury.data.remote.model.GeminiRequest
import com.dynodevv.mercury.data.remote.model.OpenAiRequest
import com.dynodevv.mercury.domain.model.AiProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val settingsDataStore: SettingsDataStore
) {
    companion object {
        private const val TAG = "AiRepository"
    }

    fun streamChatCompletion(messages: List<AiMessage>): Flow<String> = flow {
        val provider = settingsDataStore.getAiProviderConfig().first()
        if (provider == null) {
            emit("Error: No AI provider configured. Please add your API key in Settings.")
            return@flow
        }

        when (provider.provider) {
            "gemini" -> {
                emitAll(streamGemini(provider, messages))
            }
            else -> {
                emitAll(streamOpenAiCompatible(provider, messages))
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun streamOpenAiCompatible(provider: AiProvider, messages: List<AiMessage>): Flow<String> = flow {
        val endpoint = provider.customEndpoint?.takeIf { it.isNotBlank() }
            ?: when (provider.provider) {
                "groq" -> "https://api.groq.com/openai/v1/chat/completions"
                else -> "https://api.openai.com/v1/chat/completions"
            }

        val requestBody = OpenAiRequest(
            model = provider.model,
            messages = messages,
            stream = true
        )

        try {
            httpClient.preparePost(endpoint) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${provider.apiKey}")
                    append(HttpHeaders.Accept, "text/event-stream")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.execute { response ->
                val channel = response.bodyAsChannel()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") break

                        try {
                            val json = Json.decodeFromString<JsonObject>(data)
                            val choicesArray = json["choices"] as? JsonArray
                            val firstChoice = choicesArray?.firstOrNull() as? JsonObject
                            val delta = firstChoice?.get("delta")?.jsonObject
                            val content = delta?.get("content")?.jsonPrimitive?.content
                            if (!content.isNullOrBlank()) {
                                emit(content)
                            }
                        } catch (e: Exception) {
                            // ignore malformed JSON chunks
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenAI-compatible streaming error", e)
            emit("Error: ${e.message}")
        }
    }

    private suspend fun streamGemini(provider: AiProvider, messages: List<AiMessage>): Flow<String> = flow {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${provider.model}:generateContent?key=${provider.apiKey}"

        val contents = messages.map { msg ->
            GeminiContent(
                role = if (msg.role == "assistant") "model" else msg.role,
                parts = listOf(GeminiPart(text = msg.content))
            )
        }

        val requestBody = GeminiRequest(contents = contents)

        try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val geminiResponse = response.body<com.dynodevv.mercury.data.remote.model.GeminiResponse>()
            val text = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                emit(text)
            } else {
                emit("Error: No response from Gemini.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini error", e)
            emit("Error: ${e.message}")
        }
    }
}

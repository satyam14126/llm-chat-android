package com.llmchat.app.data.remote.api

import com.llmchat.app.data.remote.dto.ChatCompletionRequest
import com.llmchat.app.data.remote.dto.StreamChunk
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import kotlinx.serialization.encodeToString

sealed class StreamEvent {
    data class Token(val content: String) : StreamEvent()
    data class Done(val finishReason: String?) : StreamEvent()
    data class Error(val message: String, val isRetryable: Boolean = false) : StreamEvent()
}

class StreamingApiClient(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {

    fun streamChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: ChatCompletionRequest
    ): Flow<StreamEvent> = callbackFlow {
        val streamingRequest = request.copy(stream = true)
        val requestBody = json.encodeToString(streamingRequest)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1/chat/completions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == "[DONE]") {
                    trySend(StreamEvent.Done(null))
                    return
                }
                try {
                    val chunk = json.decodeFromString<StreamChunk>(data)
                    val content = chunk.choices.firstOrNull()?.delta?.content
                    val finishReason = chunk.choices.firstOrNull()?.finishReason
                    if (content != null) {
                        trySend(StreamEvent.Token(content))
                    }
                    if (finishReason != null && finishReason != "null") {
                        trySend(StreamEvent.Done(finishReason))
                    }
                } catch (e: Exception) {
                    // Skip malformed SSE chunks
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                val isRetryable = response?.code?.let { it in 429..503 } ?: true
                val message = when {
                    response?.code == 401 -> "Authentication failed. Check your API key."
                    response?.code == 429 -> "Rate limit exceeded. Please wait and try again."
                    response?.code == 503 -> "Service temporarily unavailable."
                    t != null -> "Connection error: ${t.message}"
                    else -> "Stream failed with code ${response?.code}"
                }
                trySend(StreamEvent.Error(message, isRetryable))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(httpRequest, listener)

        awaitClose { eventSource.cancel() }
    }
}

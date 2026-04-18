package com.llmchat.app.data.repository

import com.llmchat.app.data.remote.api.LLMApiService
import com.llmchat.app.data.remote.api.StreamEvent
import com.llmchat.app.data.remote.api.StreamingApiClient
import com.llmchat.app.data.remote.dto.ChatCompletionRequest
import com.llmchat.app.data.remote.dto.ChatMessage
import com.llmchat.app.di.ApiServiceFactory
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import com.llmchat.app.domain.model.ProviderProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class LLMResult {
    data class Success(val content: String, val tokensUsed: Int = 0) : LLMResult()
    data class Failure(val message: String, val isRetryable: Boolean = false) : LLMResult()
}

@Singleton
class LLMRepository @Inject constructor(
    private val apiServiceFactory: ApiServiceFactory,
    private val streamingClient: StreamingApiClient
) {

    suspend fun sendMessage(
        profile: ProviderProfile,
        messages: List<Message>,
        systemPrompt: String = ""
    ): LLMResult {
        return try {
            val service: LLMApiService = apiServiceFactory.create(profile.baseUrl, profile.apiKey)
            val chatMessages = buildMessageList(messages, systemPrompt)

            val response = service.createChatCompletion(
                ChatCompletionRequest(
                    model = profile.model,
                    messages = chatMessages,
                    temperature = profile.temperature,
                    maxTokens = profile.maxTokens,
                    stream = false
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content ?: ""
                val tokens = body?.usage?.totalTokens ?: 0
                LLMResult.Success(content, tokens)
            } else {
                val code = response.code()
                LLMResult.Failure(
                    message = httpErrorMessage(code),
                    isRetryable = code in 429..503
                )
            }
        } catch (e: Exception) {
            LLMResult.Failure(
                message = "Network error: ${e.message ?: "Unknown error"}",
                isRetryable = true
            )
        }
    }

    fun streamMessage(
        profile: ProviderProfile,
        messages: List<Message>,
        systemPrompt: String = ""
    ): Flow<StreamEvent> {
        val chatMessages = buildMessageList(messages, systemPrompt)
        val request = ChatCompletionRequest(
            model = profile.model,
            messages = chatMessages,
            temperature = profile.temperature,
            maxTokens = profile.maxTokens,
            stream = true
        )
        return streamingClient.streamChatCompletion(profile.baseUrl, profile.apiKey, request)
    }

    suspend fun listModels(profile: ProviderProfile): List<String> {
        return try {
            val service = apiServiceFactory.create(profile.baseUrl, profile.apiKey)
            val response = service.listModels()
            if (response.isSuccessful) {
                response.body()?.data?.map { it.id } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildMessageList(messages: List<Message>, systemPrompt: String): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        if (systemPrompt.isNotBlank()) {
            result.add(ChatMessage(role = "system", content = systemPrompt))
        }
        messages.filter { !it.isSummarized && it.role != MessageRole.SYSTEM }.forEach { msg ->
            result.add(ChatMessage(role = msg.role.value, content = msg.content))
        }
        return result
    }

    private fun httpErrorMessage(code: Int): String = when (code) {
        400 -> "Bad request. Check your parameters."
        401 -> "Authentication failed. Check your API key."
        403 -> "Access forbidden."
        404 -> "API endpoint not found. Check the base URL."
        429 -> "Rate limit exceeded. Please wait and try again."
        500 -> "Server error. Please try again."
        503 -> "Service unavailable. Please try again later."
        else -> "Request failed with code $code"
    }
}

package com.llmchat.app.data.remote.api

import com.llmchat.app.data.remote.dto.ChatCompletionRequest
import com.llmchat.app.data.remote.dto.ChatCompletionResponse
import com.llmchat.app.data.remote.dto.ModelsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LLMApiService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @GET("v1/models")
    suspend fun listModels(): Response<ModelsResponse>
}

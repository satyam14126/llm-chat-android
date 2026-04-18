package com.llmchat.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.llmchat.app.data.remote.api.LLMApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiServiceFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    private val cache = mutableMapOf<String, LLMApiService>()

    fun create(baseUrl: String, apiKey: String): LLMApiService {
        val cacheKey = "$baseUrl:${apiKey.hashCode()}"
        return cache.getOrPut(cacheKey) {
            val authenticatedClient = okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Authorization", "Bearer $apiKey")
                        .header("Content-Type", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .client(authenticatedClient)
                .addConverterFactory(
                    json.asConverterFactory("application/json; charset=UTF8".toMediaType())
                )
                .build()
                .create(LLMApiService::class.java)
        }
    }

    fun invalidate(baseUrl: String, apiKey: String) {
        val cacheKey = "$baseUrl:${apiKey.hashCode()}"
        cache.remove(cacheKey)
    }
}

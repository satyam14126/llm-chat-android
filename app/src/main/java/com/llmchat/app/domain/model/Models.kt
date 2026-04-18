package com.llmchat.app.domain.model

import com.llmchat.app.data.local.entities.*

data class ChatSession(
    val id: Long = 0,
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val providerProfileId: Long = 0,
    val systemPrompt: String = "",
    val totalTokens: Int = 0,
    val isArchived: Boolean = false
)

data class Message(
    val id: Long = 0,
    val sessionId: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val tokenCount: Int = 0,
    val isError: Boolean = false,
    val isSummarized: Boolean = false,
    val attachedFiles: List<String> = emptyList(),
    val modelUsed: String = ""
)

enum class MessageRole(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system")
}

data class ProviderProfile(
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val streamingEnabled: Boolean = true,
    val systemPrompt: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AttachedFile(
    val id: Long = 0,
    val sessionId: Long,
    val fileName: String,
    val mimeType: String,
    val extractedText: String,
    val fileSize: Long,
    val addedAt: Long = System.currentTimeMillis()
)

fun ChatSessionEntity.toDomain() = ChatSession(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt,
    providerProfileId = providerProfileId, systemPrompt = systemPrompt,
    totalTokens = totalTokens, isArchived = isArchived
)

fun ChatSession.toEntity() = ChatSessionEntity(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt,
    providerProfileId = providerProfileId, systemPrompt = systemPrompt,
    totalTokens = totalTokens, isArchived = isArchived
)

fun MessageEntity.toDomain() = Message(
    id = id, sessionId = sessionId,
    role = when (role) {
        "user" -> MessageRole.USER
        "assistant" -> MessageRole.ASSISTANT
        else -> MessageRole.SYSTEM
    },
    content = content, createdAt = createdAt, tokenCount = tokenCount,
    isError = isError, isSummarized = isSummarized,
    attachedFiles = if (attachedFiles.isBlank()) emptyList() else attachedFiles.split(","),
    modelUsed = modelUsed
)

fun Message.toEntity() = MessageEntity(
    id = id, sessionId = sessionId, role = role.value, content = content,
    createdAt = createdAt, tokenCount = tokenCount, isError = isError,
    isSummarized = isSummarized,
    attachedFiles = attachedFiles.joinToString(","),
    modelUsed = modelUsed
)

fun ProviderProfileEntity.toDomain() = ProviderProfile(
    id = id, name = name, baseUrl = baseUrl, apiKey = apiKey, model = model,
    temperature = temperature, maxTokens = maxTokens, streamingEnabled = streamingEnabled,
    systemPrompt = systemPrompt, isDefault = isDefault, createdAt = createdAt
)

fun ProviderProfile.toEntity() = ProviderProfileEntity(
    id = id, name = name, baseUrl = baseUrl, apiKey = apiKey, model = model,
    temperature = temperature, maxTokens = maxTokens, streamingEnabled = streamingEnabled,
    systemPrompt = systemPrompt, isDefault = isDefault, createdAt = createdAt
)

fun AttachedFileEntity.toDomain() = AttachedFile(
    id = id, sessionId = sessionId, fileName = fileName, mimeType = mimeType,
    extractedText = extractedText, fileSize = fileSize, addedAt = addedAt
)

fun AttachedFile.toEntity() = AttachedFileEntity(
    id = id, sessionId = sessionId, fileName = fileName, mimeType = mimeType,
    extractedText = extractedText, fileSize = fileSize, addedAt = addedAt
)

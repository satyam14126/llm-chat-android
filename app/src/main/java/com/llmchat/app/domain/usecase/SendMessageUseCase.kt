package com.llmchat.app.domain.usecase

import com.llmchat.app.data.remote.api.StreamEvent
import com.llmchat.app.data.repository.ChatRepository
import com.llmchat.app.data.repository.LLMRepository
import com.llmchat.app.data.repository.LLMResult
import com.llmchat.app.data.repository.ProviderRepository
import com.llmchat.app.domain.model.AttachedFile
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import com.llmchat.app.domain.model.ProviderProfile
import com.llmchat.app.util.FileExtractor
import com.llmchat.app.util.TokenCounter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class SendResult {
    object Loading : SendResult()
    data class Token(val content: String) : SendResult()
    data class Done(val fullContent: String, val tokenCount: Int) : SendResult()
    data class Error(val message: String, val isRetryable: Boolean = false) : SendResult()
}

class SendMessageUseCase @Inject constructor(
    private val chatRepo: ChatRepository,
    private val llmRepo: LLMRepository,
    private val providerRepo: ProviderRepository,
    private val tokenCounter: TokenCounter,
    private val fileExtractor: FileExtractor
) {

    suspend operator fun invoke(
        sessionId: Long,
        userContent: String,
        profile: ProviderProfile,
        attachedFiles: List<AttachedFile> = emptyList()
    ): Flow<SendResult> = flow {
        emit(SendResult.Loading)

        // Build context with file chunks if needed
        val enrichedContent = if (attachedFiles.isNotEmpty()) {
            buildContextWithFiles(userContent, attachedFiles)
        } else {
            userContent
        }

        // Save user message
        val userTokens = tokenCounter.estimateTokens(enrichedContent)
        val userMsgId = chatRepo.insertMessage(
            Message(
                sessionId = sessionId,
                role = MessageRole.USER,
                content = enrichedContent,
                tokenCount = userTokens,
                attachedFiles = attachedFiles.map { it.fileName }
            )
        )

        // Get all conversation messages for context
        val allMessages = chatRepo.getMessagesOnce(sessionId)
            .filter { !it.isError }

        val systemPrompt = profile.systemPrompt

        if (profile.streamingEnabled) {
            // Streaming mode
            val streamFlow = llmRepo.streamMessage(profile, allMessages, systemPrompt)
            val fullContent = StringBuilder()

            streamFlow.collect { event ->
                when (event) {
                    is StreamEvent.Token -> {
                        fullContent.append(event.content)
                        emit(SendResult.Token(event.content))
                    }
                    is StreamEvent.Done -> {
                        val assistantTokens = tokenCounter.estimateTokens(fullContent.toString())
                        chatRepo.insertMessage(
                            Message(
                                sessionId = sessionId,
                                role = MessageRole.ASSISTANT,
                                content = fullContent.toString(),
                                tokenCount = assistantTokens,
                                modelUsed = profile.model
                            )
                        )
                        val total = chatRepo.getTotalTokens(sessionId)
                        chatRepo.updateSessionMeta(sessionId, total)
                        emit(SendResult.Done(fullContent.toString(), assistantTokens))
                    }
                    is StreamEvent.Error -> {
                        emit(SendResult.Error(event.message, event.isRetryable))
                    }
                }
            }
        } else {
            // Non-streaming mode
            val result = llmRepo.sendMessage(profile, allMessages, systemPrompt)
            when (result) {
                is LLMResult.Success -> {
                    val assistantTokens = tokenCounter.estimateTokens(result.content)
                    chatRepo.insertMessage(
                        Message(
                            sessionId = sessionId,
                            role = MessageRole.ASSISTANT,
                            content = result.content,
                            tokenCount = if (result.tokensUsed > 0) result.tokensUsed else assistantTokens,
                            modelUsed = profile.model
                        )
                    )
                    val total = chatRepo.getTotalTokens(sessionId)
                    chatRepo.updateSessionMeta(sessionId, total)
                    emit(SendResult.Done(result.content, result.tokensUsed))
                }
                is LLMResult.Failure -> {
                    emit(SendResult.Error(result.message, result.isRetryable))
                }
            }
        }
    }

    private fun buildContextWithFiles(
        userMessage: String,
        files: List<AttachedFile>
    ): String {
        val fileContext = files.joinToString("\n\n") { file ->
            val chunk = fileExtractor.getRelevantChunks(file.extractedText, userMessage)
            "### File: ${file.fileName}\n```\n$chunk\n```"
        }
        return "$userMessage\n\n---\nContext from attached files:\n\n$fileContext"
    }
}

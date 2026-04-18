package com.llmchat.app.domain.usecase

import com.llmchat.app.data.repository.ChatRepository
import com.llmchat.app.data.repository.LLMRepository
import com.llmchat.app.data.repository.LLMResult
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import com.llmchat.app.domain.model.ProviderProfile
import com.llmchat.app.util.TokenCounter
import javax.inject.Inject

class SummarizeContextUseCase @Inject constructor(
    private val chatRepo: ChatRepository,
    private val llmRepo: LLMRepository,
    private val tokenCounter: TokenCounter
) {

    companion object {
        const val CONTEXT_WARNING_THRESHOLD = 0.75f
        const val SUMMARIZE_OLDEST_COUNT = 10
    }

    suspend fun checkAndSummarizeIfNeeded(
        sessionId: Long,
        profile: ProviderProfile
    ): Boolean {
        val totalTokens = chatRepo.getTotalTokens(sessionId)
        val threshold = (profile.maxTokens * CONTEXT_WARNING_THRESHOLD).toInt()

        if (totalTokens < threshold) return false

        return summarizeOldestMessages(sessionId, profile)
    }

    suspend fun summarizeOldestMessages(
        sessionId: Long,
        profile: ProviderProfile
    ): Boolean {
        val oldMessages = chatRepo.getOldestMessages(sessionId, SUMMARIZE_OLDEST_COUNT)
        if (oldMessages.size < 3) return false

        val messagesToSummarize = oldMessages.filter { it.role != MessageRole.SYSTEM }
        if (messagesToSummarize.isEmpty()) return false

        val summaryPrompt = buildSummaryPrompt(messagesToSummarize)

        val summaryMessages = listOf(
            Message(
                sessionId = sessionId,
                role = MessageRole.USER,
                content = summaryPrompt
            )
        )

        val result = llmRepo.sendMessage(
            profile = profile.copy(maxTokens = 500, streamingEnabled = false),
            messages = summaryMessages,
            systemPrompt = "You are a concise summarizer. Summarize the conversation in 2-3 sentences."
        )

        return when (result) {
            is LLMResult.Success -> {
                val summaryTokens = tokenCounter.estimateTokens(result.content)
                // Insert a summary message
                chatRepo.insertMessage(
                    Message(
                        sessionId = sessionId,
                        role = MessageRole.SYSTEM,
                        content = "[Summary of earlier conversation]: ${result.content}",
                        tokenCount = summaryTokens
                    )
                )
                // Mark old messages as summarized
                chatRepo.markAsSummarized(messagesToSummarize.map { it.id })
                true
            }
            else -> false
        }
    }

    private fun buildSummaryPrompt(messages: List<Message>): String {
        val conversation = messages.joinToString("\n") { msg ->
            "${msg.role.value.uppercase()}: ${msg.content}"
        }
        return "Please summarize this conversation:\n\n$conversation"
    }
}

package com.llmchat.app.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenCounter @Inject constructor() {

    fun estimateTokens(text: String): Int {
        // Rough approximation: ~4 chars per token for English
        if (text.isBlank()) return 0
        val wordCount = text.trim().split(Regex("\\s+")).size
        val charCount = text.length
        // Use the larger estimate between word-based (1.3 tokens/word) and char-based
        val wordBased = (wordCount * 1.3).toInt()
        val charBased = charCount / 4
        return maxOf(wordBased, charBased)
    }

    fun estimateMessagesTokens(messages: List<Pair<String, String>>): Int {
        // Each message has ~4 tokens overhead (role, formatting)
        return messages.sumOf { (role, content) ->
            estimateTokens(role) + estimateTokens(content) + 4
        } + 2 // conversation overhead
    }

    fun fitsInContext(
        currentTokens: Int,
        newTokens: Int,
        maxContextTokens: Int,
        reservedForResponse: Int = 1024
    ): Boolean = (currentTokens + newTokens) < (maxContextTokens - reservedForResponse)
}

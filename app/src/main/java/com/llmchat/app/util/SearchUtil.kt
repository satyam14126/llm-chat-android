package com.llmchat.app.util

import com.llmchat.app.domain.model.Message

/**
 * Utility for searching through messages
 */
object SearchUtil {
    
    /**
     * Search messages by query string (case-insensitive)
     */
    fun searchMessages(
        messages: List<Message>,
        query: String
    ): List<Message> {
        if (query.isBlank()) return messages
        
        val lowerQuery = query.lowercase()
        return messages.filter { message ->
            message.content.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Get search results with context (surrounding messages)
     */
    fun searchWithContext(
        messages: List<Message>,
        query: String,
        contextLines: Int = 1
    ): List<Pair<Int, Message>> {
        if (query.isBlank()) return emptyList()
        
        val lowerQuery = query.lowercase()
        val results = mutableListOf<Pair<Int, Message>>()
        
        messages.forEachIndexed { index, message ->
            if (message.content.lowercase().contains(lowerQuery)) {
                results.add(Pair(index, message))
            }
        }
        
        return results
    }

    /**
     * Highlight search query in text
     */
    fun highlightQuery(text: String, query: String): String {
        if (query.isBlank()) return text
        
        val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
        return text.replace(regex) { "**${it.value}**" }
    }

    /**
     * Get snippet of text around the query
     */
    fun getSnippet(text: String, query: String, snippetLength: Int = 100): String {
        if (query.isBlank()) return text.take(snippetLength)
        
        val index = text.lowercase().indexOf(query.lowercase())
        if (index == -1) return text.take(snippetLength)
        
        val start = maxOf(0, index - snippetLength / 2)
        val end = minOf(text.length, start + snippetLength)
        
        val snippet = text.substring(start, end)
        return (if (start > 0) "..." else "") + snippet + (if (end < text.length) "..." else "")
    }
}

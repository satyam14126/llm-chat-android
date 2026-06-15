package com.llmchat.app.util

/**
 * Default quick reply templates for common prompts
 */
object QuickReplyManager {
    
    data class QuickReply(
        val id: String,
        val title: String,
        val template: String,
        val icon: String = "💬"
    )

    private val defaultReplies = listOf(
        QuickReply(
            id = "explain",
            title = "Explain",
            template = "Can you explain this in simple terms?",
            icon = "📚"
        ),
        QuickReply(
            id = "summarize",
            title = "Summarize",
            template = "Please summarize the key points from your previous response.",
            icon = "📝"
        ),
        QuickReply(
            id = "expand",
            title = "Expand",
            template = "Can you provide more details on this topic?",
            icon = "📖"
        ),
        QuickReply(
            id = "example",
            title = "Example",
            template = "Can you provide a concrete example?",
            icon = "💡"
        ),
        QuickReply(
            id = "code",
            title = "Code",
            template = "Can you provide code example for this?",
            icon = "💻"
        ),
        QuickReply(
            id = "alternative",
            title = "Alternative",
            template = "What are alternative approaches to this?",
            icon = "🔄"
        ),
        QuickReply(
            id = "pros_cons",
            title = "Pros & Cons",
            template = "What are the pros and cons of this approach?",
            icon = "⚖️"
        ),
        QuickReply(
            id = "best_practice",
            title = "Best Practice",
            template = "What's the best practice for this?",
            icon = "⭐"
        ),
        QuickReply(
            id = "troubleshoot",
            title = "Troubleshoot",
            template = "How can I troubleshoot this issue?",
            icon = "🔧"
        ),
        QuickReply(
            id = "performance",
            title = "Performance",
            template = "How can I optimize this for better performance?",
            icon = "⚡"
        )
    )

    fun getDefaultReplies(): List<QuickReply> = defaultReplies

    fun getReplyById(id: String): QuickReply? = defaultReplies.find { it.id == id }

    fun getReplysByCategory(category: String): List<QuickReply> {
        return when (category) {
            "learning" -> defaultReplies.filter { it.id in listOf("explain", "summarize", "expand", "example") }
            "development" -> defaultReplies.filter { it.id in listOf("code", "best_practice", "performance") }
            "problem_solving" -> defaultReplies.filter { it.id in listOf("alternative", "pros_cons", "troubleshoot") }
            else -> defaultReplies
        }
    }

    fun addCustomReply(reply: QuickReply): List<QuickReply> {
        return defaultReplies + reply
    }
}

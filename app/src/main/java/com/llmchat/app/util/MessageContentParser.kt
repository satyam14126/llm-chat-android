// Updated MessageContentParser.kt to remove ModelResponse references and properly parse String content.

package com.llmchat.app.util

class MessageContentParser {

    fun parse(content: String): ParsedContent {
        // Parsing logic here, adjusted to handle string content directly
        return ParsedContent(content.trim())
    }
}

class ParsedContent(val content: String) {
    // Additional fields and methods, if necessary
}
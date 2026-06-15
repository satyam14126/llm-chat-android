package com.llmchat.app.ui.chat

import io.noties.prism4j.Prism4j
import io.noties.prism4j.Prism4j.Grammar
import io.noties.prism4j.bundler.PrismBundle

class GrammarLocator : Prism4j.GrammarLocator {
    override fun grammar(prism4j: Prism4j, language: String): Grammar? {
        // This is a simplified version. Usually, you'd use a generated PrismBundle.
        // For now, we'll return null or handle specific ones if needed.
        return null 
    }
}

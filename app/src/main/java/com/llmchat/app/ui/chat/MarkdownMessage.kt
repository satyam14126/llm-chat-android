package com.llmchat.app.ui.chat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownComponents
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.markdownColor
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import com.mikepenz.markdown.code.highlightedCodeBlock
import com.mikepenz.markdown.code.highlightedCodeFence

@Composable
fun MarkdownMessage(
    content: String,
    modifier: Modifier = Modifier,
    color: Int? = null // Keeping for compatibility, though M3 theme is preferred
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val highlightsBuilder = remember(isDarkTheme) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkTheme))
    }

    Markdown(
        content = content,
        modifier = modifier,
        colors = markdownColor(
            text = if (color != null) Color(color) else Color.Unspecified
        ),
        components = markdownComponents(
            codeBlock = highlightedCodeBlock(highlightsBuilder = highlightsBuilder),
            codeFence = highlightedCodeFence(highlightsBuilder = highlightsBuilder)
        )
    )
}

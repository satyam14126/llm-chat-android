package com.llmchat.app.ui.chat

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.llmchat.app.R
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle

@Composable
fun MarkdownMessage(
    content: String,
    modifier: Modifier = Modifier,
    color: Int? = null
) {
    val context = LocalContext.current
    val markwon = remember(context) {
        val prism4j = Prism4j(com.llmchat.app.ui.chat.GrammarLocator())
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create()))
            .build()
    }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                color?.let { setTextColor(it) }
                // Custom styling could be added here
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, content)
        },
        modifier = modifier
    )
}

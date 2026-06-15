package com.llmchat.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E)) // Dark background for code
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = MaterialTheme.typography.labelMedium,
                color = Color.LightGray
            )
            TextButton(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp),
                    tint = Color.LightGray
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Copy",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // Using MarkdownMessage for syntax highlighting within the card
            MarkdownMessage(
                content = "```$language\n$code\n```",
                color = Color.White.toArgb()
            )
        }
    }
}

// Extension to convert Compose Color to ARGB Int
private fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
            ((this.red * 255.0f + 0.5f).toInt() shl 16) or
            ((this.green * 255.0f + 0.5f).toInt() shl 8) or
            (this.blue * 255.0f + 0.5f).toInt()
}

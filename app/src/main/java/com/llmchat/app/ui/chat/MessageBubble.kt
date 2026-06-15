package com.llmchat.app.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole

@Composable
fun MessageBubble(
    message: Message,
    onCopy: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onRegenerate: (() -> Unit)?
) {
    val isUser = remember(message.role) { message.role == MessageRole.USER }
    val isSystem = remember(message.role) { message.role == MessageRole.SYSTEM }
    val clipboard: ClipboardManager = LocalClipboardManager.current
    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember(message.content) { mutableStateOf(message.content) }
    var showActions by remember { mutableStateOf(false) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 10,
                    label = { Text("Message") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(editText)
                    showEditDialog = false
                }) { Text("Save & Resend") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (isSystem) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SmartToy,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
            }

            Surface(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    ),
                color = if (isUser) MaterialTheme.colorScheme.primary
                else if (message.isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = { showActions = !showActions }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        MarkdownMessage(
                            content = message.content,
                            color = if (message.isError) MaterialTheme.colorScheme.onErrorContainer.toArgb()
                                    else MaterialTheme.colorScheme.onSurface.toArgb()
                        )
                    }
                }
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .shadow(2.dp, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Message actions row
        AnimatedVisibility(visible = showActions) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 36.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(message.content))
                        showActions = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, "Copy", Modifier.size(16.dp))
                }
                if (isUser) {
                    IconButton(
                        onClick = { editText = message.content; showEditDialog = true; showActions = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", Modifier.size(16.dp))
                    }
                }
                if (!isUser && onRegenerate != null) {
                    IconButton(
                        onClick = { onRegenerate(); showActions = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Regenerate", Modifier.size(16.dp))
                    }
                }
                IconButton(
                    onClick = { onDelete(); showActions = false },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Attached files info
        if (message.attachedFiles.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 4.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(message.attachedFiles.size) { index ->
                    AssistChip(
                        onClick = { },
                        label = { Text(message.attachedFiles[index], style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.AttachFile, null, Modifier.size(14.dp)) }
                    )
                }
            }
        }
    }
}

private fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
            ((this.red * 255.0f + 0.5f).toInt() shl 16) or
            ((this.green * 255.0f + 0.5f).toInt() shl 8) or
            (this.blue * 255.0f + 0.5f).toInt()
}

@Composable
fun StreamingMessageBubble(content: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SmartToy,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
                ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                MarkdownMessage(
                    content = content + if (alpha > 0.5f) " ▌" else "  ",
                    color = MaterialTheme.colorScheme.onSurface.toArgb()
                )
            }
        }
    }
}

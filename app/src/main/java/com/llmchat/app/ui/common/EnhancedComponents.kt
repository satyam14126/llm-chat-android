package com.llmchat.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.llmchat.app.util.QuickReplyManager

/**
 * Enhanced search bar with clear button and animations
 */
@Composable
fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search messages...",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

/**
 * Quick reply buttons row for common prompts
 */
@Composable
fun QuickReplyRow(
    onReplySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(visible = visible) {
        Column(modifier = modifier) {
            Text(
                "Quick Replies",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(QuickReplyManager.getDefaultReplies()) { reply ->
                    AssistChip(
                        onClick = { onReplySelected(reply.template) },
                        label = { Text(reply.title) },
                        leadingIcon = { Text(reply.icon) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Search result item with context
 */
@Composable
fun SearchResultItem(
    text: String,
    query: String,
    index: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = { onSelect(index) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Result #${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text.take(150) + if (text.length > 150) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (query.isNotEmpty()) {
                Text(
                    "Matches: $query",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Loading skeleton for chat messages
 */
@Composable
fun MessageSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {}
        Spacer(Modifier.width(8.dp))
        Surface(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {}
    }
}

/**
 * Error state card with retry option
 */
@Composable
fun ErrorStateCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isDismissible: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Error,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                if (isDismissible && onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Chip group for filtering or categorization
 */
@Composable
fun ChipGroup(
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onItemSelected(item) },
                label = { Text(item) },
                leadingIcon = if (item == selectedItem) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

/**
 * Info banner for displaying tips or status
 */
@Composable
fun InfoBanner(
    message: String,
    modifier: Modifier = Modifier,
    type: BannerType = BannerType.INFO,
    dismissible: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(visible = isVisible) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (type) {
                    BannerType.INFO -> MaterialTheme.colorScheme.primaryContainer
                    BannerType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                    BannerType.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (type) {
                        BannerType.INFO -> Icons.Default.Info
                        BannerType.WARNING -> Icons.Default.Warning
                        BannerType.SUCCESS -> Icons.Default.CheckCircle
                    },
                    null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                if (dismissible) {
                    IconButton(
                        onClick = {
                            isVisible = false
                            onDismiss?.invoke()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

enum class BannerType {
    INFO, WARNING, SUCCESS
}

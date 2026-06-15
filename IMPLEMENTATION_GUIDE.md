# Implementation Guide - Enhanced LLM Chat Client

This guide explains how to integrate and use the new enhancements in your LLM Chat Android app.

## Quick Start

### 1. Theme System Integration

The theme system has been enhanced with multiple color schemes. To use it:

**Step 1: Update MainActivity.kt**

The MainActivity has been updated to pass the theme mode to the LLMChatTheme composable:

```kotlin
val settingsViewModel: SettingsViewModel = hiltViewModel()
val darkMode by settingsViewModel.darkMode.collectAsState()
val themeMode by settingsViewModel.themeMode.collectAsState()
LLMChatTheme(
    darkTheme = darkMode ?: isSystemInDarkTheme(),
    themeMode = themeMode
) {
    AppNavigation()
}
```

**Step 2: Access Theme Settings**

Users can change the theme in Settings → Appearance → Theme.

### 2. Enhanced Message Bubbles

The message bubbles now support code block highlighting. This is automatic - no changes needed. Messages containing code blocks (marked with triple backticks) will be formatted beautifully.

Example message that will be highlighted:
```
Here's a Kotlin example:
\`\`\`kotlin
fun main() {
    println("Hello, World!")
}
\`\`\`
```

### 3. Settings Enhancements

The SettingsViewModel now manages additional preferences:

```kotlin
val themeMode: StateFlow<ThemeMode> = ... // Current theme
val enableAnimations: StateFlow<Boolean> = ... // Animation toggle
val autoScroll: StateFlow<Boolean> = ... // Auto-scroll preference

// Set preferences
viewModel.setThemeMode(ThemeMode.OCEAN)
viewModel.setEnableAnimations(false)
viewModel.setAutoScroll(true)
```

## Using New Utilities

### Message Search

The SearchUtil provides comprehensive message search functionality:

```kotlin
import com.llmchat.app.util.SearchUtil

// Search messages
val query = "kotlin"
val results = SearchUtil.searchMessages(messages, query)

// Get search results with surrounding context
val contextResults = SearchUtil.searchWithContext(messages, query, contextLines = 2)

// Get a snippet of text for preview
val snippet = SearchUtil.getSnippet(messageContent, query, snippetLength = 100)

// Highlight query in text
val highlighted = SearchUtil.highlightQuery(messageContent, query)
```

### Quick Replies

The QuickReplyManager provides pre-defined reply templates:

```kotlin
import com.llmchat.app.util.QuickReplyManager

// Get all default replies
val allReplies = QuickReplyManager.getDefaultReplies()

// Get a specific reply by ID
val explainReply = QuickReplyManager.getReplyById("explain")

// Get replies by category
val learningReplies = QuickReplyManager.getReplysByCategory("learning")
val devReplies = QuickReplyManager.getReplysByCategory("development")
val problemReplies = QuickReplyManager.getReplysByCategory("problem_solving")

// Add custom reply
val customReply = QuickReplyManager.QuickReply(
    id = "custom_1",
    title = "My Custom Reply",
    template = "This is my custom reply template",
    icon = "🎯"
)
```

## Using Enhanced Components

### EnhancedSearchBar

Display a modern search input:

```kotlin
import com.llmchat.app.ui.common.EnhancedSearchBar

var searchQuery by remember { mutableStateOf("") }

EnhancedSearchBar(
    query = searchQuery,
    onQueryChange = { searchQuery = it },
    onSearch = { query ->
        // Perform search
        val results = SearchUtil.searchMessages(messages, query)
    },
    placeholder = "Search messages..."
)
```

### QuickReplyRow

Display quick reply buttons:

```kotlin
import com.llmchat.app.ui.common.QuickReplyRow

QuickReplyRow(
    onReplySelected = { template ->
        // Insert template into input
        inputText = template
    },
    visible = true
)
```

### SearchResultItem

Display search results:

```kotlin
import com.llmchat.app.ui.common.SearchResultItem

SearchResultItem(
    text = message.content,
    query = searchQuery,
    index = index,
    onSelect = { selectedIndex ->
        // Navigate to message
        scrollToMessage(selectedIndex)
    }
)
```

### ErrorStateCard

Display error messages with retry:

```kotlin
import com.llmchat.app.ui.common.ErrorStateCard

ErrorStateCard(
    message = "Failed to send message. Please check your connection.",
    onRetry = { viewModel.retrySendMessage() },
    isDismissible = true,
    onDismiss = { showError = false }
)
```

### InfoBanner

Display informational banners:

```kotlin
import com.llmchat.app.ui.common.InfoBanner
import com.llmchat.app.ui.common.BannerType

InfoBanner(
    message = "Context is approaching token limit. Consider summarizing.",
    type = BannerType.WARNING,
    dismissible = true
)
```

### ChipGroup

Display selectable chips:

```kotlin
import com.llmchat.app.ui.common.ChipGroup

var selectedFilter by remember { mutableStateOf("all") }

ChipGroup(
    items = listOf("all", "user", "assistant", "system"),
    selectedItem = selectedFilter,
    onItemSelected = { selectedFilter = it }
)
```

## Architecture Changes

### SettingsViewModel Enhancements

The SettingsViewModel now includes:

```kotlin
// New preference flows
val themeMode: StateFlow<ThemeMode>
val enableAnimations: StateFlow<Boolean>
val autoScroll: StateFlow<Boolean>

// New setter methods
fun setThemeMode(mode: ThemeMode)
fun setEnableAnimations(enabled: Boolean)
fun setAutoScroll(enabled: Boolean)

// Enhanced provider methods
fun getDefaultProfile(): StateFlow<ProviderProfile?>
```

### ProviderRepository Enhancements

```kotlin
// New flow-based method for reactive updates
fun getDefaultProfileFlow(): Flow<ProviderProfile?>
```

### ProviderProfileDao Enhancements

```kotlin
// New flow-based query
@Query("SELECT * FROM provider_profiles WHERE isDefault = 1 LIMIT 1")
fun getDefaultProfileFlow(): Flow<ProviderProfileEntity?>
```

## Integration Checklist

- [ ] Update MainActivity.kt with theme mode support
- [ ] Test theme switching in Settings
- [ ] Verify code block highlighting in messages
- [ ] Test animation toggle in Settings
- [ ] Test auto-scroll preference
- [ ] Integrate SearchUtil for message search (optional)
- [ ] Add QuickReplyRow to chat screen (optional)
- [ ] Use EnhancedComponents in appropriate screens
- [ ] Test on various devices and screen sizes
- [ ] Verify accessibility features

## Performance Considerations

1. **Search Performance**: SearchUtil uses efficient string matching. For large message lists (10,000+), consider implementing pagination.

2. **UI Rendering**: The new components use Compose's lazy loading where appropriate to maintain performance.

3. **Memory**: Theme data is stored in DataStore, which is efficient for small preference objects.

4. **Animation Performance**: Users can disable animations in Settings for better performance on lower-end devices.

## Troubleshooting

### Theme Not Changing
- Ensure SettingsViewModel is properly injected
- Check that MainActivity is collecting the themeMode state
- Verify DataStore is properly initialized

### Code Blocks Not Highlighting
- Ensure code is wrapped in triple backticks (\`\`\`)
- Verify the message content includes the code block markers
- Check that MessageBubble is rendering the RenderMessageWithCodeBlocks composable

### Quick Replies Not Appearing
- Verify QuickReplyRow is added to the chat screen
- Check that the `visible` parameter is true
- Ensure QuickReplyManager is properly imported

## Future Enhancements

Consider implementing these features in future versions:

1. **Voice Input**: Integrate speech-to-text for quick voice messages
2. **Message Reactions**: Allow emoji reactions to messages
3. **Conversation Bookmarks**: Save important messages
4. **Custom Themes**: Let users create custom color schemes
5. **Message Pinning**: Pin important messages to top
6. **Search History**: Remember recent searches
7. **Offline Support**: Cache messages for offline viewing
8. **Cloud Sync**: Synchronize settings across devices

## Support

For issues or questions about the enhancements, refer to the ENHANCEMENTS.md file for detailed information about each feature.

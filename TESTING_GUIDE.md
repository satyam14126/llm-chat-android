# Testing Guide - Enhanced LLM Chat Client

This guide covers testing strategies and best practices for the enhanced LLM Chat Client.

## 🎯 Testing Strategy

The app uses a multi-layered testing approach:

```
Unit Tests (Fast, Isolated)
    ↓
Integration Tests (Component interaction)
    ↓
UI Tests (User interactions)
    ↓
Performance Tests (Speed and memory)
```

## 🧪 Unit Tests

### Testing Utilities

**SearchUtil Tests**

```kotlin
@Test
fun testSearchMessages() {
    val messages = listOf(
        Message(id = 1, content = "Hello World"),
        Message(id = 2, content = "Goodbye World"),
        Message(id = 3, content = "Hello Again")
    )
    
    val results = SearchUtil.searchMessages(messages, "hello")
    
    assert(results.size == 2)
    assert(results[0].id == 1L)
    assert(results[1].id == 3L)
}

@Test
fun testSearchCaseInsensitive() {
    val messages = listOf(
        Message(id = 1, content = "Hello World")
    )
    
    val results = SearchUtil.searchMessages(messages, "HELLO")
    
    assert(results.size == 1)
}

@Test
fun testGetSnippet() {
    val text = "The quick brown fox jumps over the lazy dog"
    val snippet = SearchUtil.getSnippet(text, "fox", snippetLength = 20)
    
    assert(snippet.contains("fox"))
}
```

**QuickReplyManager Tests**

```kotlin
@Test
fun testGetDefaultReplies() {
    val replies = QuickReplyManager.getDefaultReplies()
    
    assert(replies.isNotEmpty())
    assert(replies.any { it.id == "explain" })
    assert(replies.any { it.id == "code" })
}

@Test
fun testGetReplyById() {
    val reply = QuickReplyManager.getReplyById("explain")
    
    assert(reply != null)
    assert(reply?.template?.isNotEmpty() == true)
}

@Test
fun testGetReplysByCategory() {
    val learningReplies = QuickReplyManager.getReplysByCategory("learning")
    
    assert(learningReplies.isNotEmpty())
    assert(learningReplies.all { it.id in listOf("explain", "summarize", "expand", "example") })
}
```

### Testing ViewModels

**ChatViewModel Tests**

```kotlin
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()

@get:Rule
val mainDispatcherRule = MainDispatcherRule()

private lateinit var chatViewModel: ChatViewModel
private val chatRepository = mockk<ChatRepository>()
private val providerRepository = mockk<ProviderRepository>()
private val llmRepository = mockk<LLMRepository>()

@Before
fun setup() {
    chatViewModel = ChatViewModel(
        chatRepo = chatRepository,
        providerRepo = providerRepository,
        llmRepo = llmRepository,
        sendMessageUseCase = mockk(),
        summarizeUseCase = mockk(),
        fileExtractor = mockk(),
        exportManager = mockk()
    )
}

@Test
fun testInitializeSession() = runTest {
    val session = ChatSession(id = 1, title = "Test")
    coEvery { chatRepository.getSessionById(1) } returns session
    
    chatViewModel.initialize(1)
    
    advanceUntilIdle()
    assert(chatViewModel.uiState.value.session == session)
}

@Test
fun testSendMessage() = runTest {
    val message = Message(id = 1, content = "Hello")
    coEvery { sendMessageUseCase.execute(any()) } returns SendResult.Success(message)
    
    chatViewModel.sendMessage("Hello")
    
    advanceUntilIdle()
    assert(chatViewModel.uiState.value.messages.contains(message))
}
```

**SettingsViewModel Tests**

```kotlin
@Test
fun testSetThemeMode() = runTest {
    val viewModel = SettingsViewModel(context, providerRepository)
    
    viewModel.setThemeMode(ThemeMode.OCEAN)
    
    advanceUntilIdle()
    assert(viewModel.themeMode.value == ThemeMode.OCEAN)
}

@Test
fun testSetAnimations() = runTest {
    val viewModel = SettingsViewModel(context, providerRepository)
    
    viewModel.setEnableAnimations(false)
    
    advanceUntilIdle()
    assert(viewModel.enableAnimations.value == false)
}
```

### Testing Repositories

**ChatRepository Tests**

```kotlin
@Test
fun testGetMessagesForSession() = runTest {
    val messages = listOf(
        Message(id = 1, content = "Hello"),
        Message(id = 2, content = "World")
    )
    coEvery { messageDao.getMessagesForSession(1) } returns flowOf(messages)
    
    val result = chatRepository.getMessagesForSession(1).first()
    
    assert(result == messages)
}

@Test
fun testInsertMessage() = runTest {
    val message = Message(id = 1, content = "Hello")
    coEvery { messageDao.insertMessage(any()) } returns 1L
    
    val id = chatRepository.insertMessage(message)
    
    assert(id == 1L)
    coVerify { messageDao.insertMessage(any()) }
}
```

## 🔗 Integration Tests

### Testing Compose UI

**MessageBubble Tests**

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testMessageBubbleRendering() {
    val message = Message(
        id = 1,
        content = "Hello World",
        role = MessageRole.USER
    )
    
    composeTestRule.setContent {
        MessageBubble(
            message = message,
            onCopy = {},
            onEdit = {},
            onDelete = {},
            onRegenerate = null
        )
    }
    
    composeTestRule.onNodeWithText("Hello World").assertIsDisplayed()
}

@Test
fun testMessageBubbleActions() {
    val message = Message(
        id = 1,
        content = "Hello World",
        role = MessageRole.USER
    )
    var copied = false
    
    composeTestRule.setContent {
        MessageBubble(
            message = message,
            onCopy = { copied = true },
            onEdit = {},
            onDelete = {},
            onRegenerate = null
        )
    }
    
    // Tap message to show actions
    composeTestRule.onNodeWithText("Hello World").performClick()
    
    // Tap copy button
    composeTestRule.onNodeWithContentDescription("Copy").performClick()
    
    assert(copied)
}
```

**EnhancedSearchBar Tests**

```kotlin
@Test
fun testSearchBarInput() {
    composeTestRule.setContent {
        var query by remember { mutableStateOf("") }
        EnhancedSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {}
        )
    }
    
    composeTestRule.onNodeWithHint("Search messages...").performTextInput("kotlin")
    
    composeTestRule.onNodeWithText("kotlin").assertIsDisplayed()
}

@Test
fun testSearchBarClear() {
    composeTestRule.setContent {
        var query by remember { mutableStateOf("kotlin") }
        EnhancedSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {}
        )
    }
    
    composeTestRule.onNodeWithContentDescription("Clear").performClick()
    
    composeTestRule.onNodeWithText("kotlin").assertDoesNotExist()
}
```

### Testing Database

**Room Database Tests**

```kotlin
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()

private lateinit var database: AppDatabase
private lateinit var messageDao: MessageDao

@Before
fun setup() {
    database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        AppDatabase::class.java
    ).allowMainThreadQueries().build()
    
    messageDao = database.messageDao()
}

@After
fun teardown() {
    database.close()
}

@Test
fun testInsertAndRetrieveMessage() = runTest {
    val message = MessageEntity(
        id = 1,
        sessionId = 1,
        role = "user",
        content = "Hello"
    )
    
    messageDao.insertMessage(message)
    val retrieved = messageDao.getMessagesForSession(1).first()
    
    assert(retrieved.isNotEmpty())
    assert(retrieved[0].content == "Hello")
}
```

## 📱 UI Tests

### Testing Chat Screen

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testChatScreenDisplaysMessages() {
    val messages = listOf(
        Message(id = 1, content = "Hello", role = MessageRole.USER),
        Message(id = 2, content = "Hi there!", role = MessageRole.ASSISTANT)
    )
    
    composeTestRule.setContent {
        ChatScreen(
            sessionId = 1,
            onNavigateBack = {},
            onNavigateToSettings = {}
        )
    }
    
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hi there!").assertIsDisplayed()
}

@Test
fun testSendMessage() {
    composeTestRule.setContent {
        ChatScreen(
            sessionId = 1,
            onNavigateBack = {},
            onNavigateToSettings = {}
        )
    }
    
    composeTestRule.onNodeWithHint("Type a message...").performTextInput("Hello")
    composeTestRule.onNodeWithContentDescription("Send").performClick()
    
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
}
```

### Testing Settings Screen

```kotlin
@Test
fun testThemeSelection() {
    composeTestRule.setContent {
        SettingsScreen(
            onNavigateBack = {},
            viewModel = mockk()
        )
    }
    
    composeTestRule.onNodeWithText("Theme").performClick()
    composeTestRule.onNodeWithText("Ocean").performClick()
    
    // Verify theme changed
    assert(viewModel.themeMode.value == ThemeMode.OCEAN)
}
```

## ⚡ Performance Tests

### Memory Profiling

```kotlin
@Test
fun testMemoryUsageWithLargeMessageList() {
    val runtime = Runtime.getRuntime()
    val memBefore = runtime.totalMemory() - runtime.freeMemory()
    
    val messages = (1..10000).map {
        Message(id = it.toLong(), content = "Message $it")
    }
    
    val memAfter = runtime.totalMemory() - runtime.freeMemory()
    val memUsed = (memAfter - memBefore) / 1024 / 1024 // MB
    
    assert(memUsed < 100) // Should use less than 100MB
}
```

### Rendering Performance

```kotlin
@Test
fun testMessageBubbleRenderingPerformance() {
    val startTime = System.currentTimeMillis()
    
    composeTestRule.setContent {
        repeat(100) {
            MessageBubble(
                message = Message(id = it.toLong(), content = "Message $it"),
                onCopy = {},
                onEdit = {},
                onDelete = {},
                onRegenerate = null
            )
        }
    }
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    
    assert(duration < 1000) // Should render in less than 1 second
}
```

## 🧬 Test Coverage

### Coverage Targets

| Component | Target | Current |
|-----------|--------|---------|
| Utilities | 90% | - |
| ViewModels | 85% | - |
| Repositories | 80% | - |
| UI Components | 70% | - |
| Overall | 80% | - |

### Running Coverage Reports

```bash
# Run tests with coverage
./gradlew testDebugUnitTestCoverage

# Generate HTML report
./gradlew jacocoTestReport

# View report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## 📋 Test Checklist

- [ ] Unit tests for all utilities
- [ ] Unit tests for all ViewModels
- [ ] Unit tests for all Repositories
- [ ] Integration tests for UI components
- [ ] Database tests
- [ ] API error handling tests
- [ ] Network error tests
- [ ] Performance tests
- [ ] Memory leak tests
- [ ] Accessibility tests
- [ ] Theme switching tests
- [ ] Message search tests
- [ ] Quick reply tests
- [ ] Error recovery tests
- [ ] Code coverage > 80%

## 🚀 Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Unit Tests Only
```bash
./gradlew testDebugUnitTest
```

### Run UI Tests Only
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test
```bash
./gradlew test --tests "com.llmchat.app.util.SearchUtilTest"
```

### Run with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## 📚 Testing Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Mockk Documentation](https://mockk.io/)
- [JUnit 4](https://junit.org/junit4/)
- [Espresso](https://developer.android.com/training/testing/espresso)

---

**Remember**: Good tests make refactoring safe and catch regressions early!

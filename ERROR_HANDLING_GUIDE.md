# Error Handling & Recovery Guide

This guide covers error handling strategies and recovery mechanisms in the enhanced LLM Chat Client.

## 🛡️ Error Handling Architecture

The app uses a layered error handling approach:

```
UI Layer (Display errors to user)
    ↓
ViewModel Layer (Handle and transform errors)
    ↓
UseCase Layer (Business logic error handling)
    ↓
Repository Layer (Data source error handling)
    ↓
Data Layer (Network and database errors)
```

## 📋 Common Error Types

### 1. Network Errors

| Error | Cause | Recovery |
|-------|-------|----------|
| Connection Timeout | Server unreachable | Retry with exponential backoff |
| Read Timeout | Server slow to respond | Increase timeout or retry |
| SSL Certificate Error | Invalid certificate | Check provider URL |
| DNS Resolution Error | Invalid domain | Verify base URL |
| No Internet | Device offline | Wait for connection |

### 2. API Errors

| Error | Code | Recovery |
|-------|------|----------|
| Unauthorized | 401 | Check API key |
| Forbidden | 403 | Check permissions |
| Not Found | 404 | Check endpoint URL |
| Rate Limited | 429 | Implement backoff |
| Server Error | 500+ | Retry with backoff |

### 3. Database Errors

| Error | Cause | Recovery |
|-------|-------|----------|
| Disk Full | Storage full | Free up space |
| Corruption | Data corruption | Rebuild database |
| Lock Timeout | Database locked | Retry operation |
| Query Error | Invalid query | Check schema |

### 4. Application Errors

| Error | Cause | Recovery |
|-------|-------|----------|
| Null Pointer | Missing data | Validate input |
| Invalid State | Wrong state | Reset state |
| Memory Error | Out of memory | Clear cache |
| File Error | File not found | Check file path |

## 🔄 Error Recovery Strategies

### 1. Retry with Exponential Backoff

```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 10000,
    block: suspend () -> T
): T {
    var delay = initialDelay
    var lastException: Exception? = null
    
    repeat(maxRetries) {
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            delay = (delay * 2).coerceAtMost(maxDelay)
            delay(delay)
        }
    }
    
    throw lastException ?: Exception("Failed after $maxRetries retries")
}
```

### 2. Graceful Degradation

```kotlin
// Try to use streaming, fall back to regular response
val response = try {
    llmRepository.sendMessageStreaming(message)
} catch (e: Exception) {
    logger.warn("Streaming failed, using regular response", e)
    llmRepository.sendMessage(message)
}
```

### 3. User-Friendly Error Messages

```kotlin
fun getErrorMessage(exception: Exception): String = when (exception) {
    is SocketTimeoutException -> "Connection timed out. Please check your internet."
    is UnknownHostException -> "Cannot reach the server. Check your URL."
    is HttpException -> when (exception.code()) {
        401 -> "Invalid API key. Please check your credentials."
        429 -> "Rate limited. Please wait before trying again."
        500 -> "Server error. Please try again later."
        else -> "Server error (${exception.code()}). Please try again."
    }
    is IOException -> "Network error. Please check your connection."
    else -> "An unexpected error occurred. Please try again."
}
```

### 4. Automatic Recovery

```kotlin
// Automatically retry failed messages
class ChatViewModel : ViewModel() {
    private val failedMessages = mutableListOf<Message>()
    
    fun retryFailedMessages() {
        failedMessages.forEach { message ->
            sendMessage(message)
        }
        failedMessages.clear()
    }
    
    private fun handleSendError(message: Message, error: Exception) {
        failedMessages.add(message)
        _uiState.update { it.copy(errorMessage = getErrorMessage(error)) }
    }
}
```

## 🎯 Error Handling Best Practices

### 1. Specific Exception Handling

```kotlin
// Good: Handle specific exceptions
try {
    val response = apiService.sendMessage(message)
} catch (e: HttpException) {
    handleHttpError(e)
} catch (e: IOException) {
    handleNetworkError(e)
} catch (e: Exception) {
    handleUnexpectedError(e)
}

// Bad: Catch all exceptions
try {
    val response = apiService.sendMessage(message)
} catch (e: Exception) {
    // Too generic, hard to debug
}
```

### 2. Logging Errors

```kotlin
// Good: Log with context
logger.error("Failed to send message", exception, 
    mapOf("messageId" to message.id, "providerId" to profile.id))

// Bad: No context
logger.error("Error", exception)
```

### 3. Error State Management

```kotlin
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val errorMessage: String? = null,
    val isRetryable: Boolean = false,
    val lastError: Exception? = null
)

// Update state with error
_uiState.update { 
    it.copy(
        errorMessage = getErrorMessage(exception),
        isRetryable = isRetryable(exception),
        lastError = exception
    )
}
```

### 4. Cleanup on Error

```kotlin
// Good: Clean up resources on error
try {
    val file = openFile()
    processFile(file)
} catch (e: Exception) {
    logger.error("Error processing file", e)
} finally {
    file?.close() // Always clean up
}

// Bad: No cleanup
try {
    val file = openFile()
    processFile(file)
} catch (e: Exception) {
    logger.error("Error", e)
}
```

## 🚨 Enhanced Error Components

### ErrorStateCard

Display errors with retry option:

```kotlin
ErrorStateCard(
    message = "Failed to send message. Please check your connection.",
    onRetry = { viewModel.retrySendMessage() },
    isDismissible = true,
    onDismiss = { showError = false }
)
```

### InfoBanner

Display warnings and status:

```kotlin
InfoBanner(
    message = "Context is approaching token limit.",
    type = BannerType.WARNING,
    dismissible = true
)
```

## 🔧 Error Handling Patterns

### 1. Result Pattern

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    class Loading<T> : Result<T>()
}

// Usage
when (val result = sendMessage(message)) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> handleError(result.exception)
    is Result.Loading -> showLoading()
}
```

### 2. Either Pattern

```kotlin
sealed class Either<L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>()
    data class Right<L, R>(val value: R) : Either<L, R>()
}

// Usage
val result: Either<Exception, Message> = sendMessage(message)
result.fold(
    onLeft = { handleError(it) },
    onRight = { handleSuccess(it) }
)
```

### 3. Try-Catch Pattern

```kotlin
suspend fun sendMessage(message: Message): Message? {
    return try {
        llmRepository.sendMessage(message)
    } catch (e: Exception) {
        logger.error("Failed to send message", e)
        null
    }
}
```

## 📊 Error Monitoring

### Key Metrics to Track

| Metric | Target | Action |
|--------|--------|--------|
| Error Rate | <1% | Investigate if exceeded |
| Retry Success Rate | >80% | Improve retry logic |
| Average Error Recovery Time | <5s | Optimize recovery |
| User-Reported Errors | <0.1% | Fix critical issues |

### Error Logging

```kotlin
// Log all errors with context
data class ErrorLog(
    val timestamp: Long,
    val message: String,
    val exception: Exception,
    val context: Map<String, Any>,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    INFO, WARNING, ERROR, CRITICAL
}
```

## 🎓 Error Handling Checklist

- [ ] Handle all network errors
- [ ] Handle all API errors
- [ ] Handle database errors
- [ ] Provide user-friendly error messages
- [ ] Implement retry logic
- [ ] Log errors with context
- [ ] Clean up resources on error
- [ ] Use specific exception handling
- [ ] Implement error state management
- [ ] Test error scenarios
- [ ] Monitor error rates
- [ ] Document error handling
- [ ] Use enhanced error components
- [ ] Implement graceful degradation
- [ ] Test recovery mechanisms

## 🧪 Testing Error Scenarios

### Unit Tests

```kotlin
@Test
fun testErrorHandling() {
    // Mock API error
    coEvery { apiService.sendMessage(any()) } throws HttpException(500)
    
    // Execute
    viewModel.sendMessage(message)
    
    // Verify
    assert(viewModel.uiState.value.errorMessage != null)
    assert(viewModel.uiState.value.isRetryable)
}
```

### Integration Tests

```kotlin
@Test
fun testNetworkErrorRecovery() {
    // Simulate network error
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
    
    // Send message
    val result = runBlocking { viewModel.sendMessage(message) }
    
    // Verify retry
    mockWebServer.enqueue(MockResponse().setBody("success"))
    viewModel.retryLastMessage()
    
    assert(result != null)
}
```

## 📚 Resources

- [Kotlin Coroutines Error Handling](https://kotlinlang.org/docs/exception-handling.html)
- [Android Error Handling](https://developer.android.com/guide/topics/data/data-storage)
- [Retrofit Error Handling](https://square.github.io/retrofit/)
- [Room Error Handling](https://developer.android.com/training/data-storage/room)

---

**Remember**: Good error handling improves user experience and app reliability. Always test error scenarios!

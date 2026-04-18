// Prevent duplicate Done events in StreamingApiClient.kt
fun sendDoneEvent(finishReason: Any?) {
    if (finishReason != null && !hasSentDoneEvent) {
        // send Done event
        hasSentDoneEvent = true;
    } else if (isDoneEventTriggered) {
        // Only handle [DONE] if done event hasn't been sent
        sendDoneEvent();
    }
}


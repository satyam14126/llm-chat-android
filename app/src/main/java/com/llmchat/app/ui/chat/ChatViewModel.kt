package com.llmchat.app.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.app.data.repository.ChatRepository
import com.llmchat.app.data.repository.LLMRepository
import com.llmchat.app.data.repository.ProviderRepository
import com.llmchat.app.domain.model.AttachedFile
import com.llmchat.app.domain.model.ChatSession
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import com.llmchat.app.domain.model.ProviderProfile
import com.llmchat.app.domain.usecase.SendMessageUseCase
import com.llmchat.app.domain.usecase.SendResult
import com.llmchat.app.domain.usecase.SummarizeContextUseCase
import com.llmchat.app.util.ExportImportManager
import com.llmchat.app.util.FileExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val session: ChatSession? = null,
    val messages: List<Message> = emptyList(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val isLoading: Boolean = false,
    val streamingContent: String = "",
    val errorMessage: String? = null,
    val isRetryable: Boolean = false,
    val currentProfile: ProviderProfile? = null,
    val contextWarning: Boolean = false,
    val pendingAttachments: List<AttachedFile> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val providerRepo: ProviderRepository,
    private val llmRepo: LLMRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val summarizeUseCase: SummarizeContextUseCase,
    private val fileExtractor: FileExtractor,
    private val exportManager: ExportImportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var sessionId: Long = -1L

    fun initialize(sessionId: Long) {
        this.sessionId = sessionId
        viewModelScope.launch {
            val session = chatRepo.getSessionById(sessionId)
            val profile = getActiveProfile(session)
            _uiState.update { it.copy(session = session, currentProfile = profile) }
        }

        viewModelScope.launch {
            chatRepo.getMessagesForSession(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }

        viewModelScope.launch {
            chatRepo.getFilesForSession(sessionId).collect { files ->
                _uiState.update { it.copy(attachedFiles = files) }
            }
        }
    }

    private suspend fun getActiveProfile(session: ChatSession?): ProviderProfile? {
        if (session != null && session.providerProfileId > 0) {
            providerRepo.getProfileById(session.providerProfileId)?.let { return it }
        }
        return providerRepo.getDefaultProfile()
    }

    fun sendMessage(content: String) {
        val profile = _uiState.value.currentProfile ?: run {
            _uiState.update { it.copy(errorMessage = "No provider configured. Please add a provider in Settings.") }
            return
        }

        if (_uiState.value.isLoading) return

        val pendingFiles = _uiState.value.pendingAttachments
        _uiState.update { it.copy(isLoading = true, errorMessage = null, streamingContent = "", pendingAttachments = emptyList()) }

        viewModelScope.launch {
            // Check context limits
            val totalTokens = chatRepo.getTotalTokens(sessionId)
            val warningThreshold = (profile.maxTokens * 0.75f).toInt()
            if (totalTokens > warningThreshold) {
                _uiState.update { it.copy(contextWarning = true) }
            }

            sendMessageUseCase(sessionId, content, profile, pendingFiles).collect { result ->
                when (result) {
                    is SendResult.Loading -> {}
                    is SendResult.Token -> {
                        _uiState.update { it.copy(streamingContent = it.streamingContent + result.content) }
                    }
                    is SendResult.Done -> {
                        _uiState.update { it.copy(isLoading = false, streamingContent = "") }
                        autoTitleSession()
                    }
                    is SendResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                streamingContent = "",
                                errorMessage = result.message,
                                isRetryable = result.isRetryable
                            )
                        }
                    }
                }
            }
        }
    }

    fun retryLastMessage() {
        val messages = _uiState.value.messages
        val lastUserMsg = messages.lastOrNull { it.role == MessageRole.USER } ?: return
        viewModelScope.launch {
            // Delete failed assistant message if present
            val lastMsg = messages.lastOrNull()
            if (lastMsg?.isError == true || lastMsg?.role == MessageRole.ASSISTANT) {
                chatRepo.deleteMessage(lastMsg.id)
            }
            // Delete and resend user message
            chatRepo.deleteMessage(lastUserMsg.id)
            sendMessage(lastUserMsg.content)
        }
    }

    fun editMessage(messageId: Long, newContent: String) {
        viewModelScope.launch {
            val message = _uiState.value.messages.firstOrNull { it.id == messageId } ?: return@launch
            // Delete all messages from this point forward
            chatRepo.deleteMessagesFrom(sessionId, messageId)
            // Resend with new content
            sendMessage(newContent)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch { chatRepo.deleteMessage(messageId) }
    }

    fun regenerateResponse() {
        val messages = _uiState.value.messages
        val lastAssistant = messages.lastOrNull { it.role == MessageRole.ASSISTANT } ?: return
        viewModelScope.launch {
            chatRepo.deleteMessage(lastAssistant.id)
            val lastUser = messages.lastOrNull { it.role == MessageRole.USER } ?: return@launch
            chatRepo.deleteMessage(lastUser.id)
            sendMessage(lastUser.content)
        }
    }

    fun attachFile(uri: Uri) {
        viewModelScope.launch {
            val result = fileExtractor.extractFromUri(uri)
            result.onSuccess { content ->
                val file = AttachedFile(
                    sessionId = sessionId,
                    fileName = content.fileName,
                    mimeType = content.mimeType,
                    extractedText = content.text,
                    fileSize = content.fileSize
                )
                val fileId = chatRepo.insertFile(file)
                val savedFile = file.copy(id = fileId)
                _uiState.update { it.copy(pendingAttachments = it.pendingAttachments + savedFile) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Failed to read file: ${e.message}") }
            }
        }
    }

    fun removePendingAttachment(file: AttachedFile) {
        viewModelScope.launch {
            chatRepo.deleteFile(file.id)
            _uiState.update { it.copy(pendingAttachments = it.pendingAttachments - file) }
        }
    }

    fun summarizeContext() {
        val profile = _uiState.value.currentProfile ?: return
        viewModelScope.launch {
            val success = summarizeUseCase.summarizeOldestMessages(sessionId, profile)
            if (success) {
                _uiState.update { it.copy(contextWarning = false) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissContextWarning() {
        _uiState.update { it.copy(contextWarning = false) }
    }

    fun renameSession(newTitle: String) {
        viewModelScope.launch { chatRepo.renameSession(sessionId, newTitle) }
    }

    private suspend fun autoTitleSession() {
        val session = _uiState.value.session ?: return
        if (session.title != "New Chat") return
        val messages = _uiState.value.messages
        val firstUser = messages.firstOrNull { it.role == MessageRole.USER } ?: return
        val autoTitle = firstUser.content.take(40).trim()
            .replace("\n", " ")
            .let { if (it.length == 40) "$it..." else it }
        chatRepo.renameSession(sessionId, autoTitle)
    }

    fun exportChat(format: ExportFormat, onSuccess: (android.net.Uri, String) -> Unit) {
        val session = _uiState.value.session ?: return
        val messages = _uiState.value.messages
        viewModelScope.launch {
            val (uri, mimeType) = when (format) {
                ExportFormat.JSON -> exportManager.exportToJson(session, messages) to "application/json"
                ExportFormat.MARKDOWN -> exportManager.exportToMarkdown(session, messages) to "text/markdown"
                ExportFormat.TEXT -> exportManager.exportToText(session, messages) to "text/plain"
            }
            onSuccess(uri, mimeType)
        }
    }
}

enum class ExportFormat { JSON, MARKDOWN, TEXT }

package com.llmchat.app.data.repository

import android.net.Uri
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import com.llmchat.app.util.ExportImportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepository @Inject constructor(
    private val exportImportManager: ExportImportManager,
    private val chatRepository: ChatRepository
) {

    suspend fun importFromBackup(uri: Uri): Result<Long> {
        val exportResult = exportImportManager.importFromJson(uri)
        exportResult.onFailure { return Result.failure(it) }

        val export = exportResult.getOrThrow()
        val sessionId = chatRepository.createSession(
            title = "[Imported] ${export.session.title}",
            providerProfileId = 0
        )

        export.messages.forEach { msgExport ->
            chatRepository.insertMessage(
                Message(
                    sessionId = sessionId,
                    role = when (msgExport.role) {
                        "user" -> MessageRole.USER
                        "assistant" -> MessageRole.ASSISTANT
                        else -> MessageRole.SYSTEM
                    },
                    content = msgExport.content,
                    createdAt = msgExport.createdAt,
                    tokenCount = msgExport.tokenCount
                )
            )
        }

        return Result.success(sessionId)
    }
}

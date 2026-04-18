package com.llmchat.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.llmchat.app.domain.model.ChatSession
import com.llmchat.app.domain.model.Message
import com.llmchat.app.domain.model.MessageRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ChatExport(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val session: SessionExport,
    val messages: List<MessageExport>
)

@Serializable
data class SessionExport(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val systemPrompt: String
)

@Serializable
data class MessageExport(
    val id: Long,
    val role: String,
    val content: String,
    val createdAt: Long,
    val tokenCount: Int
)

@Singleton
class ExportImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {

    suspend fun exportToJson(session: ChatSession, messages: List<Message>): Uri =
        withContext(Dispatchers.IO) {
            val export = ChatExport(
                session = SessionExport(
                    id = session.id,
                    title = session.title,
                    createdAt = session.createdAt,
                    systemPrompt = session.systemPrompt
                ),
                messages = messages.map {
                    MessageExport(
                        id = it.id,
                        role = it.role.value,
                        content = it.content,
                        createdAt = it.createdAt,
                        tokenCount = it.tokenCount
                    )
                }
            )
            val jsonStr = json.encodeToString(export)
            val fileName = "chat_${sanitizeTitle(session.title)}_${timestamp()}.json"
            writeToFile(fileName, jsonStr)
        }

    suspend fun exportToMarkdown(session: ChatSession, messages: List<Message>): Uri =
        withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.appendLine("# ${session.title}")
            sb.appendLine()
            sb.appendLine("*Exported: ${formatDate(System.currentTimeMillis())}*")
            sb.appendLine()
            if (session.systemPrompt.isNotBlank()) {
                sb.appendLine("**System Prompt:** ${session.systemPrompt}")
                sb.appendLine()
            }
            sb.appendLine("---")
            sb.appendLine()
            messages.forEach { msg ->
                val roleLabel = when (msg.role) {
                    MessageRole.USER -> "**You**"
                    MessageRole.ASSISTANT -> "**Assistant**"
                    MessageRole.SYSTEM -> "**System**"
                }
                sb.appendLine("$roleLabel  ")
                sb.appendLine("*${formatDate(msg.createdAt)}*")
                sb.appendLine()
                sb.appendLine(msg.content)
                sb.appendLine()
                sb.appendLine("---")
                sb.appendLine()
            }
            val fileName = "chat_${sanitizeTitle(session.title)}_${timestamp()}.md"
            writeToFile(fileName, sb.toString())
        }

    suspend fun exportToText(session: ChatSession, messages: List<Message>): Uri =
        withContext(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.appendLine(session.title)
            sb.appendLine("Exported: ${formatDate(System.currentTimeMillis())}")
            sb.appendLine("=".repeat(50))
            sb.appendLine()
            messages.forEach { msg ->
                val roleLabel = when (msg.role) {
                    MessageRole.USER -> "YOU"
                    MessageRole.ASSISTANT -> "ASSISTANT"
                    MessageRole.SYSTEM -> "SYSTEM"
                }
                sb.appendLine("[$roleLabel] ${formatDate(msg.createdAt)}")
                sb.appendLine(msg.content)
                sb.appendLine()
            }
            val fileName = "chat_${sanitizeTitle(session.title)}_${timestamp()}.txt"
            writeToFile(fileName, sb.toString())
        }

    suspend fun importFromJson(uri: Uri): Result<ChatExport> = withContext(Dispatchers.IO) {
        try {
            val text = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                ?: return@withContext Result.failure(Exception("Could not read file"))
            val export = json.decodeFromString<ChatExport>(text)
            Result.success(export)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid backup file: ${e.message}"))
        }
    }

    fun createShareIntent(uri: Uri, mimeType: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writeToFile(fileName: String, content: String): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(content)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun sanitizeTitle(title: String): String =
        title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)

    private fun timestamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
}

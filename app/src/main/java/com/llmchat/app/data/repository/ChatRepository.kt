package com.llmchat.app.data.repository

import com.llmchat.app.data.local.dao.ChatSessionDao
import com.llmchat.app.data.local.dao.MessageDao
import com.llmchat.app.data.local.dao.AttachedFileDao
import com.llmchat.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: MessageDao,
    private val fileDao: AttachedFileDao
) {

    fun getAllSessions(): Flow<List<ChatSession>> =
        sessionDao.getAllSessions().map { it.map { e -> e.toDomain() } }

    fun searchSessions(query: String): Flow<List<ChatSession>> =
        sessionDao.searchSessions(query).map { it.map { e -> e.toDomain() } }

    suspend fun getSessionById(id: Long): ChatSession? =
        sessionDao.getSessionById(id)?.toDomain()

    suspend fun createSession(title: String = "New Chat", providerProfileId: Long = 0): Long {
        val entity = ChatSession(
            title = title,
            providerProfileId = providerProfileId
        ).toEntity()
        return sessionDao.insertSession(entity)
    }

    suspend fun updateSession(session: ChatSession) =
        sessionDao.updateSession(session.toEntity())

    suspend fun renameSession(id: Long, title: String) =
        sessionDao.renameSession(id, title)

    suspend fun deleteSession(id: Long) =
        sessionDao.deleteSession(id)

    fun getMessagesForSession(sessionId: Long): Flow<List<Message>> =
        messageDao.getMessagesForSession(sessionId).map { it.map { e -> e.toDomain() } }

    suspend fun getMessagesOnce(sessionId: Long): List<Message> =
        messageDao.getMessagesForSessionOnce(sessionId).map { it.toDomain() }

    suspend fun insertMessage(message: Message): Long =
        messageDao.insertMessage(message.toEntity())

    suspend fun updateMessage(message: Message) =
        messageDao.updateMessage(message.toEntity())

    suspend fun deleteMessage(id: Long) =
        messageDao.deleteMessage(id)

    suspend fun editMessage(id: Long, content: String) =
        messageDao.editMessage(id, content)

    suspend fun deleteMessagesFrom(sessionId: Long, fromId: Long) =
        messageDao.deleteMessagesFrom(sessionId, fromId)

    suspend fun getTotalTokens(sessionId: Long): Int =
        messageDao.getTotalTokensForSession(sessionId) ?: 0

    suspend fun getOldestMessages(sessionId: Long, limit: Int): List<Message> =
        messageDao.getOldestMessages(sessionId, limit).map { it.toDomain() }

    suspend fun markAsSummarized(ids: List<Long>) =
        messageDao.markAsSummarized(ids)

    fun getFilesForSession(sessionId: Long): Flow<List<AttachedFile>> =
        fileDao.getFilesForSession(sessionId).map { it.map { e -> e.toDomain() } }

    suspend fun getFilesOnce(sessionId: Long): List<AttachedFile> =
        fileDao.getFilesForSessionOnce(sessionId).map { it.toDomain() }

    suspend fun insertFile(file: AttachedFile): Long =
        fileDao.insertFile(file.toEntity())

    suspend fun deleteFile(id: Long) =
        fileDao.deleteFile(id)

    suspend fun updateSessionMeta(sessionId: Long, tokens: Int) {
        sessionDao.updateSessionMeta(sessionId, System.currentTimeMillis(), tokens)
    }
}

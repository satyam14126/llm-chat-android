package com.llmchat.app.data.local.dao

import androidx.room.*
import com.llmchat.app.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getMessagesForSessionOnce(sessionId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("UPDATE messages SET content = :content WHERE id = :id")
    suspend fun editMessage(id: Long, content: String)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId AND id >= :fromId")
    suspend fun deleteMessagesFrom(sessionId: Long, fromId: Long)

    @Query("SELECT SUM(tokenCount) FROM messages WHERE sessionId = :sessionId AND isSummarized = 0")
    suspend fun getTotalTokensForSession(sessionId: Long): Int?

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND isSummarized = 0 ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getOldestMessages(sessionId: Long, limit: Int): List<MessageEntity>

    @Query("UPDATE messages SET isSummarized = 1 WHERE id IN (:ids)")
    suspend fun markAsSummarized(ids: List<Long>)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteAllMessagesForSession(sessionId: Long)
}

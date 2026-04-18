package com.llmchat.app.data.local.dao

import androidx.room.*
import com.llmchat.app.data.local.entities.AttachedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachedFileDao {

    @Query("SELECT * FROM attached_files WHERE sessionId = :sessionId ORDER BY addedAt DESC")
    fun getFilesForSession(sessionId: Long): Flow<List<AttachedFileEntity>>

    @Query("SELECT * FROM attached_files WHERE sessionId = :sessionId ORDER BY addedAt DESC")
    suspend fun getFilesForSessionOnce(sessionId: Long): List<AttachedFileEntity>

    @Insert
    suspend fun insertFile(file: AttachedFileEntity): Long

    @Query("DELETE FROM attached_files WHERE id = :id")
    suspend fun deleteFile(id: Long)
}

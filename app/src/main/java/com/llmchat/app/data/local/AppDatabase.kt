package com.llmchat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.llmchat.app.data.local.dao.*
import com.llmchat.app.data.local.entities.*

@Database(
    entities = [
        ChatSessionEntity::class,
        MessageEntity::class,
        ProviderProfileEntity::class,
        AttachedFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun messageDao(): MessageDao
    abstract fun providerProfileDao(): ProviderProfileDao
    abstract fun attachedFileDao(): AttachedFileDao
}

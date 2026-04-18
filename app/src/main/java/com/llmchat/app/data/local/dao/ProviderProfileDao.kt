package com.llmchat.app.data.local.dao

import androidx.room.*
import com.llmchat.app.data.local.entities.ProviderProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderProfileDao {

    @Query("SELECT * FROM provider_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<ProviderProfileEntity>>

    @Query("SELECT * FROM provider_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ProviderProfileEntity?

    @Query("SELECT * FROM provider_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultProfile(): ProviderProfileEntity?

    @Insert
    suspend fun insertProfile(profile: ProviderProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProviderProfileEntity)

    @Query("DELETE FROM provider_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("UPDATE provider_profiles SET isDefault = 0")
    suspend fun clearDefaultProfile()

    @Query("UPDATE provider_profiles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultProfile(id: Long)
}

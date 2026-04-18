package com.llmchat.app.data.repository

import com.llmchat.app.data.local.dao.ProviderProfileDao
import com.llmchat.app.domain.model.ProviderProfile
import com.llmchat.app.domain.model.toDomain
import com.llmchat.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    private val dao: ProviderProfileDao
) {
    fun getAllProfiles(): Flow<List<ProviderProfile>> =
        dao.getAllProfiles().map { it.map { e -> e.toDomain() } }

    suspend fun getProfileById(id: Long): ProviderProfile? =
        dao.getProfileById(id)?.toDomain()

    suspend fun getDefaultProfile(): ProviderProfile? =
        dao.getDefaultProfile()?.toDomain()

    suspend fun insertProfile(profile: ProviderProfile): Long =
        dao.insertProfile(profile.toEntity())

    suspend fun updateProfile(profile: ProviderProfile) =
        dao.updateProfile(profile.toEntity())

    suspend fun deleteProfile(id: Long) =
        dao.deleteProfile(id)

    suspend fun setDefaultProfile(id: Long) {
        dao.clearDefaultProfile()
        dao.setDefaultProfile(id)
    }
}

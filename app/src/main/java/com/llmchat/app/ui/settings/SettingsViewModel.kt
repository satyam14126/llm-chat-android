package com.llmchat.app.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.app.data.repository.ProviderRepository
import com.llmchat.app.domain.model.ProviderProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerRepo: ProviderRepository
) : ViewModel() {

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    val profiles: StateFlow<List<ProviderProfile>> = providerRepo.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val darkMode: StateFlow<Boolean?> = context.dataStore.data
        .map { it[DARK_MODE_KEY] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
        }
    }

    fun addProfile(profile: ProviderProfile) {
        viewModelScope.launch {
            val id = providerRepo.insertProfile(profile)
            // If it's the first profile, set as default
            val current = providerRepo.getDefaultProfile()
            if (current == null) {
                providerRepo.setDefaultProfile(id)
            }
        }
    }

    fun updateProfile(profile: ProviderProfile) {
        viewModelScope.launch { providerRepo.updateProfile(profile) }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch { providerRepo.deleteProfile(id) }
    }

    fun setDefaultProfile(id: Long) {
        viewModelScope.launch { providerRepo.setDefaultProfile(id) }
    }
}

package com.llmchat.app.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmchat.app.data.repository.ChatRepository
import com.llmchat.app.data.repository.ProviderRepository
import com.llmchat.app.domain.model.ChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val providerRepo: ProviderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<ChatSession>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) chatRepo.getAllSessions()
            else chatRepo.searchSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createNewSession(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val profileId = providerRepo.getDefaultProfile()?.id ?: 0L
            val sessionId = chatRepo.createSession(
                title = "New Chat",
                providerProfileId = profileId
            )
            onCreated(sessionId)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { chatRepo.deleteSession(id) }
    }

    fun renameSession(id: Long, newTitle: String) {
        viewModelScope.launch { chatRepo.renameSession(id, newTitle) }
    }
}

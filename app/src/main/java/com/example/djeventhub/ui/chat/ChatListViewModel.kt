package com.example.djeventhub.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.models.Chat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(
        val chats: List<Chat>,
        val totalUnreadCount: Int,
        val isRefreshing: Boolean = false
    ) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Success(emptyList(), 0, true))
    val uiState: StateFlow<ChatListUiState> = _uiState

    private var observeJob: Job? = null
    private var isFirstLoad = true

    init {
        android.util.Log.d("ChatListViewModel", "ViewModel initialized")
        observeChats()
    }

    private fun observeChats() {
        // Don't cancel existing job - keep listening to real-time updates
        if (observeJob?.isActive == true) {
            android.util.Log.d("ChatListViewModel", "Chat observation already active")
            return
        }

        android.util.Log.d("ChatListViewModel", "Starting chat observation")
        observeJob = viewModelScope.launch {
            try {
                chatRepository.observeUserChats().collect { chats ->
                    android.util.Log.d("ChatListViewModel", "Received ${chats.size} chats from repository")

                    // Calculate unread count from chats list directly
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val totalUnread = chats.sumOf { it.getUnreadCount(currentUserId) }

                    // On first load, show data immediately without loading state
                    if (isFirstLoad) {
                        isFirstLoad = false
                        android.util.Log.d("ChatListViewModel", "First load complete")
                    }

                    _uiState.value = ChatListUiState.Success(
                        chats = chats,
                        totalUnreadCount = totalUnread,
                        isRefreshing = false
                    )
                    android.util.Log.d("ChatListViewModel", "UI State updated: ${chats.size} chats, unread=$totalUnread")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatListViewModel", "Error observing chats", e)
                // Only show error if we don't have data yet
                if (_uiState.value !is ChatListUiState.Success ||
                    (_uiState.value as? ChatListUiState.Success)?.chats?.isEmpty() == true) {
                    _uiState.value = ChatListUiState.Error(e.message ?: "Error al cargar chats")
                }
            }
        }
    }

    fun refresh() {
        android.util.Log.d("ChatListViewModel", "Refresh requested")
        val current = _uiState.value
        if (current is ChatListUiState.Success) {
            // Just update the refreshing flag, keep the data
            _uiState.value = current.copy(isRefreshing = true)
            android.util.Log.d("ChatListViewModel", "Set isRefreshing=true")

            // The Flow is already listening, data will update automatically
            // Just reset the refreshing flag after a short delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                val state = _uiState.value
                if (state is ChatListUiState.Success && state.isRefreshing) {
                    _uiState.value = state.copy(isRefreshing = false)
                    android.util.Log.d("ChatListViewModel", "Set isRefreshing=false")
                }
            }
        }
    }
}
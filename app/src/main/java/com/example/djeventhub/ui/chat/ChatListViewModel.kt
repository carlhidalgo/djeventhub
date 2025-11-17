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

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    private var observeJob: Job? = null

    init {
        observeChats(initial = true)
    }

    private fun observeChats(initial: Boolean = false) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            if (initial) _uiState.value = ChatListUiState.Loading
            try {
                chatRepository.observeUserChats().collect { chats ->
                    val totalUnread = chatRepository.getTotalUnreadCount()
                    _uiState.value = ChatListUiState.Success(
                        chats = chats,
                        totalUnreadCount = totalUnread,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Error al cargar chats")
            }
        }
    }

    fun refresh() {
        val current = _uiState.value
        if (current is ChatListUiState.Success) {
            _uiState.value = current.copy(isRefreshing = true)
        }
        observeChats(initial = false)
    }
}
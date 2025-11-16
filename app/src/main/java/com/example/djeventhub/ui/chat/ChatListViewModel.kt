package com.example.djeventhub.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.models.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val chats: List<Chat>, val totalUnreadCount: Int) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
}

class ChatListViewModel : ViewModel() {
    private val chatRepository = ChatRepository()

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    init {
        observeChats()
    }

    private fun observeChats() {
        viewModelScope.launch {
            try {
                chatRepository.observeUserChats().collect { chats ->
                    val totalUnread = chatRepository.getTotalUnreadCount()
                    _uiState.value = ChatListUiState.Success(chats, totalUnread)
                }
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Error al cargar chats")
            }
        }
    }

    fun refresh() {
        observeChats()
    }
}

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
                android.util.Log.d("ChatListViewModel", "Starting to observe chats")
                chatRepository.observeUserChats().collect { chats ->
                    android.util.Log.d("ChatListViewModel", "Received ${chats.size} chats from repository")
                    chats.forEachIndexed { index, chat ->
                        android.util.Log.d("ChatListViewModel", "Chat $index: id=${chat.chatId}, participants=${chat.participantIds}, lastMessage=${chat.lastMessage}")
                    }
                    val totalUnread = chatRepository.getTotalUnreadCount()
                    _uiState.value = ChatListUiState.Success(
                        chats = chats,
                        totalUnreadCount = totalUnread,
                        isRefreshing = false
                    )
                    android.util.Log.d("ChatListViewModel", "UI State updated with ${chats.size} chats")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatListViewModel", "Error observing chats: ${e.message}", e)
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
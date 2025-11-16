package com.example.djeventhub.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.models.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<Message>, val currentUserId: String) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(private val chatId: String) : ViewModel() {
    private val chatRepository = ChatRepository()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    init {
        observeMessages()
        markAsRead()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            try {
                chatRepository.observeChatMessages(chatId).collect { messages ->
                    _uiState.value = ChatUiState.Success(messages, currentUserId)
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Error al cargar mensajes")
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                chatRepository.sendMessage(chatId, text)
                _messageText.value = ""
            } catch (e: Exception) {
                // Handle error silently or show toast
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            try {
                chatRepository.markMessagesAsRead(chatId)
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
}

// Factory for ChatViewModel
class ChatViewModelFactory(private val chatId: String) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

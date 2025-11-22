package com.example.djeventhub.ui.productora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.data.EventRepository
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _applicants = MutableStateFlow<List<User>>(emptyList())
    val applicants: StateFlow<List<User>> = _applicants

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult

    fun loadApplicants(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get event to retrieve applicant IDs
                val event = eventRepository.getEventById(eventId)

                if (event != null && event.applicants.isNotEmpty()) {
                    // Fetch user profiles for each applicant
                    val applicantProfiles = mutableListOf<User>()

                    event.applicants.forEach { userId ->
                        val user = userRepository.getUserById(userId)
                        if (user != null) {
                            applicantProfiles.add(user)
                        }
                    }

                    // Sort by rating (highest first)
                    _applicants.value = applicantProfiles.sortedByDescending { it.rating }
                } else {
                    _applicants.value = emptyList()
                }
            } catch (e: Exception) {
                _applicants.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptApplicant(eventId: String, djId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("ApplicationsViewModel", "acceptApplicant: eventId=$eventId, djId=$djId")
            try {
                val result = eventRepository.acceptApplication(eventId, djId)
                result.fold(
                    onSuccess = {
                        android.util.Log.d("ApplicationsViewModel", "Event application accepted, now creating chat")
                        // After accepting, ensure a chat exists between productora and the DJ
                        try {
                            val djProfile = userRepository.getUserById(djId)
                            val otherName = djProfile?.artistName ?: djProfile?.displayName ?: "DJ"
                            val otherImage = djProfile?.profileImageUrl
                            android.util.Log.d("ApplicationsViewModel", "Creating chat with: name=$otherName, image=$otherImage")
                            val chatId = chatRepository.getOrCreateChat(djId, otherName, otherImage)
                            android.util.Log.d("ApplicationsViewModel", "Chat created/retrieved: $chatId")
                            _actionResult.value = "accepted:$djId,chat:$chatId"
                        } catch (e: Exception) {
                            android.util.Log.e("ApplicationsViewModel", "Error creating chat", e)
                            // If chat creation fails, still mark accepted but surface warning
                            _actionResult.value = "accepted:$djId,chat_error:${e.message}"
                        }
                    },
                    onFailure = { e ->
                        android.util.Log.e("ApplicationsViewModel", "Error accepting application", e)
                        _actionResult.value = "error:${e.message}"
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("ApplicationsViewModel", "Unexpected error in acceptApplicant", e)
                _actionResult.value = "error:${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getOrCreateChat(otherUserId: String, otherUserName: String, otherUserImage: String?): String? {
        return try {
            chatRepository.getOrCreateChat(otherUserId, otherUserName, otherUserImage)
        } catch (e: Exception) {
            null
        }
    }
}
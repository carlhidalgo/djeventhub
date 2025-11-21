package com.example.djeventhub.ui.productora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.EventRepository
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _applicants = MutableStateFlow<List<User>>(emptyList())
    val applicants: StateFlow<List<User>> = _applicants

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
}

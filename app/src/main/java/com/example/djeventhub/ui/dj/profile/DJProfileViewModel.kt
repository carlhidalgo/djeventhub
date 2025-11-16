package com.example.djeventhub.ui.dj.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DJProfileUiState {
    object Loading : DJProfileUiState()
    data class Success(val user: User) : DJProfileUiState()
    data class Error(val message: String) : DJProfileUiState()
}

class DJProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow<DJProfileUiState>(DJProfileUiState.Loading)
    val uiState: StateFlow<DJProfileUiState> = _uiState

    init {
        // Start observing profile in real-time
        startObservingProfile()
    }

    private fun startObservingProfile() {
        viewModelScope.launch {
            _uiState.value = DJProfileUiState.Loading
            try {
                userRepository.observeCurrentUserProfile().collect { user ->
                    if (user != null) {
                        _uiState.value = DJProfileUiState.Success(user)
                    } else {
                        _uiState.value = DJProfileUiState.Error("No se encontró el perfil")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadProfile() {
        // Kept for manual refresh if needed
        viewModelScope.launch {
            _uiState.value = DJProfileUiState.Loading
            try {
                val user = userRepository.getCurrentUserProfile()
                if (user != null) {
                    _uiState.value = DJProfileUiState.Success(user)
                } else {
                    _uiState.value = DJProfileUiState.Error("No se encontró el perfil")
                }
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateProfile(
        artistName: String,
        bio: String,
        musicGenres: List<String>,
        availableDays: List<String>,
        phone: String?,
        location: String?
    ) {
        viewModelScope.launch {
            _uiState.value = DJProfileUiState.Loading
            try {
                val currentUser = userRepository.getCurrentUserProfile()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        artistName = artistName,
                        bio = bio,
                        musicGenres = musicGenres,
                        availableDays = availableDays,
                        phone = phone,
                        location = location
                    )
                    userRepository.updateUserProfile(updatedUser)
                    // Real-time observer will emit the updated user
                } else {
                    _uiState.value = DJProfileUiState.Error("No se encontró el usuario")
                }
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(e.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = DJProfileUiState.Loading
            try {
                val urlResult = userRepository.uploadProfileImage(uri)
                urlResult.fold(onSuccess = { url ->
                    viewModelScope.launch {
                        val user = userRepository.getCurrentUserProfile()
                        if (user != null) {
                            userRepository.updateUserProfile(user.copy(profileImageUrl = url))
                            // Real-time observer will update UI state
                        } else {
                            _uiState.value = DJProfileUiState.Error("No se encontró el usuario")
                        }
                    }
                }, onFailure = { e ->
                    _uiState.value = DJProfileUiState.Error(
                        e.message ?: "Error subiendo imagen"
                    )
                })
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(
                    e.message ?: "Error subiendo imagen"
                )
            }
        }
    }
}
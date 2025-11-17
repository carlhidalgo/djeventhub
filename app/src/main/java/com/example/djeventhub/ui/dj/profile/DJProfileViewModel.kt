package com.example.djeventhub.ui.dj.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DJProfileUiState {
    object Loading : DJProfileUiState()
    data class Success(val user: User) : DJProfileUiState()
    data class Error(val message: String) : DJProfileUiState()
}

@HiltViewModel
class DJProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DJProfileUiState>(DJProfileUiState.Loading)
    val uiState: StateFlow<DJProfileUiState> = _uiState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

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
                    val result = userRepository.updateUserProfile(updatedUser)
                    result.fold(onSuccess = {
                        _message.value = "Perfil actualizado"
                    }, onFailure = { e ->
                        _message.value = e.message ?: "Error al actualizar"
                    })
                    // Real-time observer actualizará Success/Error
                } else {
                    _uiState.value = DJProfileUiState.Error("No se encontró el usuario")
                    _message.value = "Usuario inexistente"
                }
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(e.message ?: "Error al actualizar perfil")
                _message.value = e.message ?: "Error desconocido"
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
                            val result = userRepository.updateUserProfile(user.copy(profileImageUrl = url))
                            result.fold(onSuccess = {
                                _message.value = "Foto actualizada"
                            }, onFailure = { e ->
                                _message.value = e.message ?: "Error guardando foto"
                            })
                        } else {
                            _uiState.value = DJProfileUiState.Error("No se encontró el usuario")
                            _message.value = "Usuario inexistente"
                        }
                    }
                }, onFailure = { e ->
                    _uiState.value = DJProfileUiState.Error(e.message ?: "Error subiendo imagen")
                    _message.value = e.message ?: "Error subiendo imagen"
                })
            } catch (e: Exception) {
                _uiState.value = DJProfileUiState.Error(e.message ?: "Error subiendo imagen")
                _message.value = e.message ?: "Error subiendo imagen"
            }
        }
    }

    fun consumeMessage() { _message.value = null }
}
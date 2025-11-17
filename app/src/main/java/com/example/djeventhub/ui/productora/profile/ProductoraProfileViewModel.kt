package com.example.djeventhub.ui.productora.profile

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

sealed class ProductoraProfileUiState {
    object Loading : ProductoraProfileUiState()
    data class Success(val user: User) : ProductoraProfileUiState()
    data class Error(val message: String) : ProductoraProfileUiState()
}

@HiltViewModel
class ProductoraProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductoraProfileUiState>(ProductoraProfileUiState.Loading)
    val uiState: StateFlow<ProductoraProfileUiState> = _uiState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        startObservingProfile()
    }

    private fun startObservingProfile() {
        viewModelScope.launch {
            _uiState.value = ProductoraProfileUiState.Loading
            try {
                userRepository.observeCurrentUserProfile().collect { user ->
                    if (user != null) {
                        _uiState.value = ProductoraProfileUiState.Success(user)
                    } else {
                        _uiState.value = ProductoraProfileUiState.Error("No se encontró el perfil")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductoraProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateProfile(
        companyName: String,
        description: String,
        phone: String?,
        location: String?
    ) {
        viewModelScope.launch {
            _uiState.value = ProductoraProfileUiState.Loading
            try {
                val currentUser = userRepository.getCurrentUserProfile()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        companyName = companyName,
                        description = description,
                        phone = phone,
                        location = location
                    )
                    val result = userRepository.updateUserProfile(updatedUser)
                    result.fold(onSuccess = {
                        _message.value = "Perfil actualizado"
                    }, onFailure = { e ->
                        _message.value = e.message ?: "Error al actualizar"
                    })
                } else {
                    _uiState.value = ProductoraProfileUiState.Error("No se encontró el usuario")
                    _message.value = "Usuario inexistente"
                }
            } catch (e: Exception) {
                _uiState.value = ProductoraProfileUiState.Error(e.message ?: "Error al actualizar perfil")
                _message.value = e.message ?: "Error desconocido"
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ProductoraProfileUiState.Loading
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
                            _uiState.value = ProductoraProfileUiState.Error("No se encontró el usuario")
                            _message.value = "Usuario inexistente"
                        }
                    }
                }, onFailure = { e ->
                    _uiState.value = ProductoraProfileUiState.Error(e.message ?: "Error subiendo imagen")
                    _message.value = e.message ?: "Error subiendo imagen"
                })
            } catch (e: Exception) {
                _uiState.value = ProductoraProfileUiState.Error(e.message ?: "Error subiendo imagen")
                _message.value = e.message ?: "Error subiendo imagen"
            }
        }
    }

    fun consumeMessage() { _message.value = null }
}

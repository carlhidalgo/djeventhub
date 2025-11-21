package com.example.djeventhub.ui.dj.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PublicDJProfileUiState {
    object Loading : PublicDJProfileUiState()
    data class Success(val user: User) : PublicDJProfileUiState()
    data class Error(val message: String) : PublicDJProfileUiState()
}

@HiltViewModel
class PublicDJProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublicDJProfileUiState>(PublicDJProfileUiState.Loading)
    val uiState: StateFlow<PublicDJProfileUiState> = _uiState

    fun loadDJProfile(djId: String) {
        viewModelScope.launch {
            _uiState.value = PublicDJProfileUiState.Loading
            try {
                val user = userRepository.getUserById(djId)
                if (user != null) {
                    _uiState.value = PublicDJProfileUiState.Success(user)
                } else {
                    _uiState.value = PublicDJProfileUiState.Error("No se encontró el perfil")
                }
            } catch (e: Exception) {
                _uiState.value = PublicDJProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

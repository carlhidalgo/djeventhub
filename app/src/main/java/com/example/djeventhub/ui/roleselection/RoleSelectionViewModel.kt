package com.example.djeventhub.ui.roleselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.UserType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RoleSelectionUiState {
    object Idle : RoleSelectionUiState()
    object Loading : RoleSelectionUiState()
    object Success : RoleSelectionUiState()
    data class Error(val message: String) : RoleSelectionUiState()
}

class RoleSelectionViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<RoleSelectionUiState>(RoleSelectionUiState.Idle)
    val uiState: StateFlow<RoleSelectionUiState> = _uiState

    fun createUserProfile(userType: UserType) {
        viewModelScope.launch {
            _uiState.value = RoleSelectionUiState.Loading

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = RoleSelectionUiState.Error("No hay usuario autenticado")
                return@launch
            }

            try {
                val result = userRepository.createUserProfile(
                    uid = currentUser.uid,
                    email = currentUser.email ?: "",
                    userType = userType,
                    displayName = currentUser.displayName ?: currentUser.email ?: "Usuario"
                )

                if (result.isSuccess) {
                    _uiState.value = RoleSelectionUiState.Success
                } else {
                    _uiState.value = RoleSelectionUiState.Error(
                        result.exceptionOrNull()?.message ?: "Error al crear perfil"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RoleSelectionUiState.Error(
                    e.message ?: "Error desconocido"
                )
            }
        }
    }
}

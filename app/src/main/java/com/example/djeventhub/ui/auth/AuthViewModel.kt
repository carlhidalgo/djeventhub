package com.example.djeventhub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.auth.AuthRepository
import com.example.djeventhub.models.UserType
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: com.example.djeventhub.data.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = authRepo.signInWithEmail(email, password)
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user.uid)
                } else {
                    _uiState.value = AuthUiState.Error("No user after sign-in")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    /**
     * Sign up with email and optionally set the user role (DJ or PRODUCTORA).
     * If userType is null, defaults to DJ to preserve previous behavior.
     */
    fun signUpWithEmail(email: String, password: String, userType: UserType? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = authRepo.signUpWithEmail(email, password)
                if (user != null) {
                    // Auto-create basic user profile in Firestore on sign-up with chosen role
                    userRepo.createUserProfile(
                        uid = user.uid,
                        email = user.email ?: "",
                        userType = userType ?: com.example.djeventhub.models.UserType.DJ,
                        displayName = user.email?.substringBefore("@") ?: "DJ"
                    )
                    _uiState.value = AuthUiState.Authenticated(user.uid)
                } else {
                    _uiState.value = AuthUiState.Error("No user after sign-up")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign-up failed")
            }
        }
    }

    fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val user = authRepo.signInWithCredential(credential)
                if (user != null) {
                    // Create profile if first time Google sign-in (default to DJ)
                    val existingProfile = userRepo.getCurrentUserProfile()
                    if (existingProfile == null) {
                        userRepo.createUserProfile(
                            uid = user.uid,
                            email = user.email ?: "",
                            userType = com.example.djeventhub.models.UserType.DJ,
                            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "DJ"
                        )
                    }
                    _uiState.value = AuthUiState.Authenticated(user.uid)
                } else {
                    _uiState.value = AuthUiState.Error("No user after Google sign-in")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }
}
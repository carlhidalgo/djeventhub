package com.example.djeventhub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { cont ->
        task.addOnCompleteListener { t ->
            if (t.isSuccessful) {
                cont.resume(t.result as T)
            } else {
                cont.resumeWithException(t.exception ?: Exception("Unknown task exception"))
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                awaitTask(auth.signInWithEmailAndPassword(email, password))
                val user = auth.currentUser
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

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                awaitTask(auth.createUserWithEmailAndPassword(email, password))
                val user = auth.currentUser
                if (user != null) {
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
                awaitTask(auth.signInWithCredential(credential))
                val user = auth.currentUser
                if (user != null) {
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
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }
}

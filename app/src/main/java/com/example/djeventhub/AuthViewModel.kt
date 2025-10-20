package com.example.djeventhub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Simple auth state for UI consumption
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Await extension for Task<T>
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown task error"))
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result: AuthResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user: FirebaseUser? = result.user
                if (user != null) {
                    _authState.value = AuthState.Success(user.uid)
                } else {
                    _authState.value = AuthState.Error("Registration failed: no user returned")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
                val user: FirebaseUser? = result.user
                if (user != null) {
                    _authState.value = AuthState.Success(user.uid)
                } else {
                    _authState.value = AuthState.Error("Login failed: no user returned")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login error")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}


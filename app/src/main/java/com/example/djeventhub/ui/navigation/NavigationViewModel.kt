package com.example.djeventhub.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Observe user profile changes
        viewModelScope.launch {
            userRepository.observeCurrentUserProfile().collect { user ->
                _currentUser.value = user
            }
        }
    }

    suspend fun getCurrentUserProfile(): User? {
        return userRepository.getCurrentUserProfile()
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}

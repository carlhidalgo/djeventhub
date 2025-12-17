package com.example.djeventhub.ui.productora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.User
import com.example.djeventhub.models.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchDJsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _djs = MutableStateFlow<List<User>>(emptyList())
    val djs: StateFlow<List<User>> = _djs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadDJs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allDJs = userRepository.getAllDJs()
                _djs.value = allDJs.sortedByDescending { it.rating }
            } catch (e: Exception) {
                _djs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

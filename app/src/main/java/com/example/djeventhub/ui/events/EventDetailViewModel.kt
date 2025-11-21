package com.example.djeventhub.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.Event
import com.example.djeventhub.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EventDetailUiState {
    object Loading : EventDetailUiState()
    data class Success(val event: Event) : EventDetailUiState()
    data class Error(val message: String) : EventDetailUiState()
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState

    private val _applyingToEvent = MutableStateFlow(false)
    val applyingToEvent: StateFlow<Boolean> = _applyingToEvent

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            try {
                val event = repository.getEventById(eventId)
                if (event != null) {
                    _uiState.value = EventDetailUiState.Success(event)
                } else {
                    _uiState.value = EventDetailUiState.Error("Evento no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = EventDetailUiState.Error(e.message ?: "Error al cargar evento")
            }
        }
    }

    fun applyToEvent(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _applyingToEvent.value = true
            try {
                val result = repository.applyToEvent(eventId)
                result.fold(
                    onSuccess = {
                        // Reload event to show updated state
                        loadEvent(eventId)
                        onSuccess()
                    },
                    onFailure = { e ->
                        onError(e.message ?: "Error al postularse")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error al postularse")
            } finally {
                _applyingToEvent.value = false
            }
        }
    }

    fun removeApplication(eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _applyingToEvent.value = true
            try {
                val result = repository.removeApplication(eventId)
                result.fold(
                    onSuccess = {
                        // Reload event to show updated state
                        loadEvent(eventId)
                        onSuccess()
                    },
                    onFailure = { e ->
                        onError(e.message ?: "Error al cancelar postulación")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error al cancelar postulación")
            } finally {
                _applyingToEvent.value = false
            }
        }
    }
}

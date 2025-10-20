package com.example.djeventhub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel that exposes a list of events
class EventListViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    fun loadEvents() {
        viewModelScope.launch {
            try {
                val result = repository.getEvents()
                _events.value = result
            } catch (e: Exception) {
                // handle or log error
                _events.value = emptyList()
            }
        }
    }
}


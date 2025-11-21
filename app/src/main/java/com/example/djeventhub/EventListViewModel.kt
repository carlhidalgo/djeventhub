package com.example.djeventhub

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.djeventhub.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel that exposes a list of events sorted by proximity
@HiltViewModel
class EventListViewModel @Inject constructor(
    private val repository: EventRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventWithDistance>>(emptyList())
    val events: StateFlow<List<EventWithDistance>> = _events

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation

    init {
        // Observe repository changes in real-time
        viewModelScope.launch {
            repository.observeEvents().collect { eventsList ->
                updateEventsWithDistance(eventsList)
            }
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                val result = repository.getEvents()
                updateEventsWithDistance(result)
            } catch (e: Exception) {
                _events.value = emptyList()
            }
        }
    }

    private suspend fun updateEventsWithDistance(eventsList: List<Event>) {
        // Try to get current location to sort by proximity
        val currentLocation = try {
            _isLoadingLocation.value = true
            locationManager.getCurrentLocation()
        } catch (e: Exception) {
            _locationError.value = "No se pudo obtener la ubicación"
            null
        } finally {
            _isLoadingLocation.value = false
        }

        // Update user location state
        currentLocation?.let {
            _userLocation.value = Pair(it.latitude, it.longitude)
        }

        // Convert events to EventWithDistance and sort
        val now = System.currentTimeMillis()
        val eventsWithDistance = eventsList
            .filter { event ->
                val effectiveEnd = event.endDate ?: event.date
                effectiveEnd >= now // keep future or ongoing events
            }
            .map { event ->
                val distance = if (currentLocation != null &&
                                  event.latitude != null &&
                                  event.longitude != null) {
                    locationManager.calculateDistance(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        event.latitude,
                        event.longitude
                    )
                } else {
                    null
                }
                EventWithDistance(event, distance)
            }

        // Sort: events with distance first (by distance), then others by date
        _events.value = eventsWithDistance.sortedWith(
            compareBy(
                { it.distanceInMeters == null }, // nulls last
                { it.distanceInMeters },          // then by distance
                { it.event.date }                 // then by date
            )
        )
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.getEvents()
                updateEventsWithDistance(result)
            } catch (e: Exception) {
                _locationError.value = "Error al refrescar eventos"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

// Data class to hold an event with its calculated distance
data class EventWithDistance(
    val event: Event,
    val distanceInMeters: Float?
) {
    val distanceText: String
        get() = when {
            distanceInMeters == null -> ""
            distanceInMeters < 1000 -> "${distanceInMeters.toInt()} m"
            else -> String.format("%.1f km", distanceInMeters / 1000)
        }
}
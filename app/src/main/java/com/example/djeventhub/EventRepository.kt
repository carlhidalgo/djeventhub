package com.example.djeventhub

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

// Repository that centralizes data access for events (Singleton for MVP)
object EventRepository {

    // In-memory storage for MVP. In production, use Firebase Firestore
    private val _events = MutableStateFlow<List<Event>>(
        listOf(
            Event(
                id = "1",
                name = "Fiesta Electrónica en Kapital",
                description = "DJ set de techno y house",
                date = System.currentTimeMillis() + 86400000, // Tomorrow
                locationName = "Teatro Kapital, Madrid",
                latitude = 40.4168,
                longitude = -3.7038,
                imageUrl = null
            ),
            Event(
                id = "2",
                name = "Noche de Reggaeton en Fabrik",
                description = "Los mejores DJs de reggaeton",
                date = System.currentTimeMillis() + 172800000, // In 2 days
                locationName = "Fabrik, Madrid",
                latitude = 40.3167,
                longitude = -3.8897,
                imageUrl = null
            ),
            Event(
                id = "3",
                name = "Sesión de Deep House",
                description = "Música chill para disfrutar",
                date = System.currentTimeMillis() + 259200000, // In 3 days
                locationName = "Sala But, Madrid",
                latitude = 40.4239,
                longitude = -3.6926,
                imageUrl = null
            ),
            Event(
                id = "4",
                name = "Festival Indie Rock",
                description = "Bandas emergentes y DJs",
                date = System.currentTimeMillis() + 432000000, // In 5 days
                locationName = "La Riviera, Madrid",
                latitude = 40.3910,
                longitude = -3.6974,
                imageUrl = null
            )
        )
    )
    val events: StateFlow<List<Event>> = _events

    suspend fun getEvents(): List<Event> {
        return withContext(Dispatchers.IO) {
            delay(300) // Simulate network delay
            _events.value
        }
    }

    suspend fun addEvent(event: Event) {
        withContext(Dispatchers.IO) {
            delay(200) // Simulate network delay
            val newEvent = event.copy(id = UUID.randomUUID().toString())
            _events.value = _events.value + newEvent
        }
    }

    suspend fun getEventDetails(id: String): Event {
        return withContext(Dispatchers.IO) {
            _events.value.first { it.id == id }
        }
    }
}


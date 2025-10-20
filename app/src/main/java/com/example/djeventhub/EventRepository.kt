package com.example.djeventhub

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Repository that centralizes data access for events
class EventRepository(private val apiService: ApiService) {

    suspend fun getEvents(): List<Event> {
        return try {
            withContext(Dispatchers.IO) {
                apiService.getEvents()
            }
        } catch (e: Exception) {
            // basic error handling: rethrow for callers to handle
            throw e
        }
    }

    suspend fun getEventDetails(id: String): Event {
        return try {
            withContext(Dispatchers.IO) {
                apiService.getEvent(id)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}


package com.example.djeventhub

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repository that centralizes data access for events using Firebase Firestore
class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val eventsCollection = firestore.collection("events")

    /**
     * Observe all events in real-time, ordered by date
     */
    fun observeEvents(): Flow<List<Event>> = callbackFlow {
        val subscription = eventsCollection
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Event(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getLong("date") ?: 0L,
                            endDate = doc.getLong("endDate"),
                            locationName = doc.getString("locationName") ?: "",
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            imageUrl = doc.getString("imageUrl"),
                            musicGenre = doc.getString("musicGenre")
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Get event by ID
     */
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = eventsCollection.document(eventId).get().await()
            if (doc.exists()) {
                Event(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: 0L,
                    endDate = doc.getLong("endDate"),
                    locationName = doc.getString("locationName") ?: "",
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    imageUrl = doc.getString("imageUrl"),
                    musicGenre = doc.getString("musicGenre")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all events (one-time fetch)
     */
    suspend fun getEvents(): List<Event> {
        return try {
            eventsCollection
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        Event(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getLong("date") ?: 0L,
                            endDate = doc.getLong("endDate"),
                            locationName = doc.getString("locationName") ?: "",
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            imageUrl = doc.getString("imageUrl"),
                            musicGenre = doc.getString("musicGenre")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Add a new event to Firestore
     */
    suspend fun addEvent(event: Event): Result<String> {
        return try {
            val eventData = hashMapOf(
                "name" to event.name,
                "description" to event.description,
                "date" to event.date,
                "endDate" to event.endDate,
                "locationName" to event.locationName,
                "latitude" to event.latitude,
                "longitude" to event.longitude,
                "imageUrl" to event.imageUrl,
                "musicGenre" to event.musicGenre,
                "createdAt" to System.currentTimeMillis()
            )

            val docRef = eventsCollection.add(eventData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get event details by ID (returns null if not found instead of crashing)
     */
    suspend fun getEventDetails(id: String): Event? {
        return try {
            val doc = eventsCollection.document(id).get().await()
            if (doc.exists()) {
                Event(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: 0L,
                    endDate = doc.getLong("endDate"),
                    locationName = doc.getString("locationName") ?: "",
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    imageUrl = doc.getString("imageUrl"),
                    musicGenre = doc.getString("musicGenre")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update an existing event
     */
    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            val eventData = hashMapOf(
                "name" to event.name,
                "description" to event.description,
                "date" to event.date,
                "endDate" to event.endDate,
                "locationName" to event.locationName,
                "latitude" to event.latitude,
                "longitude" to event.longitude,
                "imageUrl" to event.imageUrl,
                "musicGenre" to event.musicGenre
            )

            eventsCollection.document(event.id).update(eventData as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an event
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


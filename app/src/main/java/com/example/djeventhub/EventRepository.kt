package com.example.djeventhub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repository that centralizes data access for events using Firebase Firestore
class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
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
                            musicGenre = doc.getString("musicGenre"),
                            createdBy = doc.getString("createdBy") ?: "",
                            applicants = (doc.get("applicants") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            selectedDJ = doc.getString("selectedDJ")
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
                    musicGenre = doc.getString("musicGenre"),
                    createdBy = doc.getString("createdBy") ?: "",
                    applicants = (doc.get("applicants") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    selectedDJ = doc.getString("selectedDJ")
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
                            musicGenre = doc.getString("musicGenre"),
                            createdBy = doc.getString("createdBy") ?: "",
                            applicants = (doc.get("applicants") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            selectedDJ = doc.getString("selectedDJ")
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
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Usuario no autenticado"))

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
                "createdBy" to currentUserId,
                "applicants" to emptyList<String>(),
                "selectedDJ" to null,
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
     * @deprecated Use getEventById instead
     */
    @Deprecated("Use getEventById instead", ReplaceWith("getEventById(id)"))
    suspend fun getEventDetails(id: String): Event? {
        return getEventById(id)
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
                "musicGenre" to event.musicGenre,
                "applicants" to event.applicants,
                "selectedDJ" to event.selectedDJ
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

    /**
     * Apply to an event as a DJ
     */
    suspend fun applyToEvent(eventId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            // Check if event is in the future
            val eventDoc = eventsCollection.document(eventId).get().await()
            val eventDate = eventDoc.getLong("date") ?: 0L
            val endDate = eventDoc.getLong("endDate")
            val now = System.currentTimeMillis()
            val effectiveEnd = endDate ?: eventDate
            if (effectiveEnd < now) {
                return Result.failure(Exception("No puedes postular a eventos que ya pasaron"))
            }

            eventsCollection.document(eventId)
                .update("applicants", FieldValue.arrayUnion(currentUserId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove application from an event
     */
    suspend fun removeApplication(eventId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))

            eventsCollection.document(eventId)
                .update("applicants", FieldValue.arrayRemove(currentUserId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept a DJ application: set selectedDJ and update event doc
     */
    suspend fun acceptApplication(eventId: String, djId: String): Result<Unit> {
        return try {
            // Set selectedDJ and remove others? Keep applicants but set selectedDJ
            eventsCollection.document(eventId)
                .update(mapOf(
                    "selectedDJ" to djId
                ))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark event as completed and optionally increment counters (not used immediately)
     */
    suspend fun markEventCompleted(eventId: String): Result<Unit> {
        return try {
            // Implementation detail: could move to a scheduled job or manual trigger
            eventsCollection.document(eventId).update("completed", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
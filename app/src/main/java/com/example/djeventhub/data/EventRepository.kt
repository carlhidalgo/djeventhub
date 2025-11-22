package com.example.djeventhub.data

import com.example.djeventhub.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension function to convert Firestore DocumentSnapshot to Event model
 * Eliminates code duplication across the repository
 */
fun DocumentSnapshot.toEvent(): Event? {
    return try {
        toObject(Event::class.java)
    } catch (e: Exception) {
        android.util.Log.e("EventRepository", "Error converting document to Event: ${e.message}", e)
        null
    }
}

/**
 * Repository that centralizes data access for events using Firebase Firestore.
 * Uses Hilt for dependency injection.
 */
@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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
                    android.util.Log.e("EventRepository", "Error observing events: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toEvent() } ?: emptyList()
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
            doc.toEvent()
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error getting event by id: ${e.message}", e)
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
                .mapNotNull { it.toEvent() }
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error getting events: ${e.message}", e)
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
            android.util.Log.e("EventRepository", "Error adding event: ${e.message}", e)
            Result.failure(e)
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
                "musicGenre" to event.musicGenre,
                "applicants" to event.applicants,
                "selectedDJ" to event.selectedDJ
            )

            eventsCollection.document(event.id).update(eventData as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error updating event: ${e.message}", e)
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
            android.util.Log.e("EventRepository", "Error deleting event: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Apply to an event as a DJ
     */
    suspend fun applyToEvent(eventId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Check if event is in the future
            val event = getEventById(eventId)
                ?: return Result.failure(Exception("Evento no encontrado"))

            if (event.hasEnded()) {
                return Result.failure(Exception("No puedes postular a eventos que ya pasaron"))
            }

            eventsCollection.document(eventId)
                .update("applicants", FieldValue.arrayUnion(currentUserId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error applying to event: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove application from an event
     */
    suspend fun removeApplication(eventId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuario no autenticado"))

            eventsCollection.document(eventId)
                .update("applicants", FieldValue.arrayRemove(currentUserId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error removing application: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Accept a DJ application: set selectedDJ and update event doc
     */
    suspend fun acceptApplication(eventId: String, djId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId)
                .update(mapOf("selectedDJ" to djId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error accepting application: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Mark event as completed
     */
    suspend fun markEventCompleted(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).update("completed", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error marking event as completed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

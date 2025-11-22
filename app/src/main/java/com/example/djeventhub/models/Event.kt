package com.example.djeventhub.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

/**
 * Event data model for the app.
 * Represents an event that can be created by productoras and applied to by DJs.
 */
data class Event(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: Long = 0L, // Start date/time - keeping for backwards compatibility
    val endDate: Long? = null, // End date/time
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val musicGenre: String? = null, // Optional music genre
    val createdBy: String = "", // User ID of the event creator
    val applicants: List<String> = emptyList(), // List of DJ user IDs who applied
    val selectedDJ: String? = null // Selected DJ user ID (if any)
) : Serializable {
    /**
     * Check if event has ended
     */
    fun hasEnded(): Boolean {
        val now = System.currentTimeMillis()
        val effectiveEnd = endDate ?: date
        return effectiveEnd < now
    }

    /**
     * Check if event is ongoing
     */
    fun isOngoing(): Boolean {
        val now = System.currentTimeMillis()
        return date <= now && (endDate == null || endDate >= now)
    }

    /**
     * Check if event is in the future
     */
    fun isFuture(): Boolean {
        return date > System.currentTimeMillis()
    }
}

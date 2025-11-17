package com.example.djeventhub

import java.io.Serializable

// Event data model for the app
data class Event(
    val id: String,
    val name: String,
    val description: String,
    val date: Long, // Start date/time - keeping for backwards compatibility
    val endDate: Long? = null, // End date/time
    val locationName: String,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val musicGenre: String? = null, // Optional music genre
    val createdBy: String = "", // User ID of the event creator
    val applicants: List<String> = emptyList(), // List of DJ user IDs who applied
    val selectedDJ: String? = null // Selected DJ user ID (if any)
) : Serializable


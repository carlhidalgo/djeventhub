package com.example.djeventhub.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Review/Rating that a productora gives to a DJ after an event
 */
data class Review(
    @DocumentId
    val reviewId: String = "",
    val eventId: String = "", // Event where they worked together
    val reviewerId: String = "", // User who writes the review (usually productora)
    val reviewerName: String = "", // Display name of reviewer
    val reviewerImage: String? = null, // Profile image of reviewer
    val reviewedUserId: String = "", // User being reviewed (usually DJ)
    val rating: Int = 5, // 1-5 stars
    val comment: String = "",
    val photoUrls: List<String> = emptyList(), // Optional photos from the event
    @ServerTimestamp
    val createdAt: Date? = null,
    val verified: Boolean = false // true if they actually worked together (event.selectedDJ == reviewedUserId)
)

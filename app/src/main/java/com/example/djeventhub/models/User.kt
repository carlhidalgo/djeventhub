package com.example.djeventhub.models

import com.google.firebase.firestore.DocumentId

// User types in the system
enum class UserType {
    DJ,
    PRODUCTORA
}

// User profile model
data class User(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val userType: UserType? = null,
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // Common fields
    val phone: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // DJ specific fields
    val artistName: String? = null,
    val bio: String? = null,
    val musicGenres: List<String> = emptyList(),
    val availableDays: List<String> = emptyList(), // ["Monday", "Friday", ...]
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val eventsCompleted: Int = 0,

    // Productora specific fields
    val companyName: String? = null,
    val description: String? = null,
    val eventsOrganized: Int = 0
)

// Extension to check user type
fun User.isDJ() = userType == UserType.DJ
fun User.isProductora() = userType == UserType.PRODUCTORA

package com.example.djeventhub

import java.io.Serializable

// Event data model for the app
data class Event(
    val id: String,
    val name: String,
    val description: String,
    val date: Long,
    val locationName: String,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?
) : Serializable


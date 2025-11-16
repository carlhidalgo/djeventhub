package com.example.djeventhub.models

import java.io.Serializable

enum class MediaType {
    PHOTO,
    VIDEO
}

data class EventMedia(
    val id: String = "",
    val eventId: String = "",
    val filePath: String = "",
    val type: MediaType = MediaType.PHOTO,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
) : Serializable

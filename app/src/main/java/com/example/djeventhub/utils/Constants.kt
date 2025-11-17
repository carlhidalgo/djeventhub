package com.example.djeventhub.utils

/**
 * App-wide constants
 */
object Constants {

    // Time constants (milliseconds)
    const val ONE_HOUR_MS = 3600000L
    const val ONE_DAY_MS = 86400000L
    const val TWO_DAYS_MS = 172800000L
    const val THREE_DAYS_MS = 259200000L
    const val FIVE_DAYS_MS = 432000000L

    // Debounce delays (milliseconds)
    const val SEARCH_DEBOUNCE_MS = 500L
    const val NETWORK_DELAY_MS = 300L
    const val SHORT_DELAY_MS = 200L

    // UI constants
    const val ICON_SIZE_SMALL_DP = 16
    const val ICON_SIZE_MEDIUM_DP = 20
    const val ICON_SIZE_LARGE_DP = 24
    const val ICON_SIZE_XLARGE_DP = 48

    const val TOPBAR_HEIGHT_DP = 48
    const val BUTTON_HEIGHT_DP = 50
    const val IMAGE_PICKER_HEIGHT_DP = 200

    // Text field limits
    const val MIN_SEARCH_CHARS = 3
    const val MAX_DESCRIPTION_LINES = 5
    const val MAX_EVENT_NAME_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 500

    // Animation durations (milliseconds)
    const val PULSE_ANIMATION_DURATION_MS = 1500
    const val BOUNCE_ANIMATION_DURATION_MS = 100

    // Map defaults
    const val DEFAULT_MAP_ZOOM = 15f
    const val DEFAULT_MAP_HEIGHT_DP = 300

    // Default coordinates (Madrid, Spain)
    const val DEFAULT_LATITUDE = 40.4168
    const val DEFAULT_LONGITUDE = -3.7038

    // Image upload limits
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024

    // Pagination
    const val EVENTS_PER_PAGE = 20
    const val CHATS_PER_PAGE = 20
    const val MESSAGES_PER_PAGE = 50

    // Location search radius
    const val DEFAULT_SEARCH_RADIUS_KM = 50.0
    const val MAX_SEARCH_RADIUS_KM = 100.0

    // Collection names (Firestore)
    const val COLLECTION_EVENTS = "events"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_MESSAGES = "messages"

    // Storage paths (Firebase Storage)
    const val STORAGE_PATH_EVENTS = "events"
    const val STORAGE_PATH_USERS = "users"
    const val STORAGE_PROFILE_IMAGE = "profile.jpg"

    // Distance formatting
    const val METERS_PER_KM = 1000
    const val DISTANCE_DECIMAL_PLACES = 1
}

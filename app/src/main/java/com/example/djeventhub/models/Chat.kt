package com.example.djeventhub.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a chat conversation between two users
 */
data class Chat(
    @DocumentId
    val chatId: String = "",
    val participantIds: List<String> = emptyList(), // [userId1, userId2]
    val participantNames: Map<String, String> = emptyMap(), // userId -> displayName
    val participantImages: Map<String, String?> = emptyMap(), // userId -> profileImageUrl
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> unread count
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Get the other participant's ID given current user ID
    fun getOtherParticipantId(currentUserId: String): String? {
        return participantIds.firstOrNull { it != currentUserId }
    }

    // Get the other participant's name
    fun getOtherParticipantName(currentUserId: String): String {
        val otherId = getOtherParticipantId(currentUserId)
        return participantNames[otherId] ?: "Usuario"
    }

    // Get the other participant's image
    fun getOtherParticipantImage(currentUserId: String): String? {
        val otherId = getOtherParticipantId(currentUserId)
        return participantImages[otherId]
    }

    // Get unread count for current user
    fun getUnreadCount(currentUserId: String): Int {
        return unreadCount[currentUserId] ?: 0
    }
}

/**
 * Represents a single message in a chat
 */
data class Message(
    @DocumentId
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null,
    val read: Boolean = false
)

package com.example.djeventhub.data

import com.example.djeventhub.models.Chat
import com.example.djeventhub.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    private val chatsCollection = firestore.collection("chats")

    /**
     * Get or create a chat between two users
     */
    suspend fun getOrCreateChat(otherUserId: String, otherUserName: String, otherUserImage: String?): String {
        val currentUserId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val currentUser = userRepository.getCurrentUserProfile()
            ?: throw IllegalStateException("User profile not found")

        // Create a consistent chat ID (sorted user IDs)
        val chatId = listOf(currentUserId, otherUserId).sorted().joinToString("_")

        // Check if chat already exists
        val existingChat = chatsCollection.document(chatId).get().await()

        if (!existingChat.exists()) {
            // Create new chat with all fields initialized
            val chatData = hashMapOf(
                "chatId" to chatId,
                "participantIds" to listOf(currentUserId, otherUserId),
                "participantNames" to mapOf(
                    currentUserId to (currentUser.artistName ?: currentUser.displayName),
                    otherUserId to otherUserName
                ),
                "participantImages" to mapOf(
                    currentUserId to currentUser.profileImageUrl,
                    otherUserId to otherUserImage
                ),
                "unreadCount" to mapOf(
                    currentUserId to 0,
                    otherUserId to 0
                ),
                "lastMessage" to "",
                "lastMessageSenderId" to "",
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            chatsCollection.document(chatId).set(chatData).await()
        }

        return chatId
    }

    /**
     * Observe all chats for current user in real-time
     */
    fun observeUserChats(): Flow<List<Chat>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = chatsCollection
            .whereArrayContains("participantIds", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(chats)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Observe messages in a chat in real-time
     */
    fun observeChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = chatsCollection
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Send a message in a chat
     */
    suspend fun sendMessage(chatId: String, text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUser = userRepository.getCurrentUserProfile() ?: return

        val message = Message(
            chatId = chatId,
            senderId = currentUserId,
            senderName = currentUser.artistName ?: currentUser.displayName,
            text = text,
            read = false
        )

        // Add message to subcollection
        val messageRef = chatsCollection
            .document(chatId)
            .collection("messages")
            .document()

        messageRef.set(message.copy(messageId = messageRef.id)).await()

        // Update chat with last message
        val chat = chatsCollection.document(chatId).get().await().toObject(Chat::class.java)
        if (chat != null) {
            val otherUserId = chat.getOtherParticipantId(currentUserId)
            val updatedUnreadCount = chat.unreadCount.toMutableMap()
            if (otherUserId != null) {
                updatedUnreadCount[otherUserId] = (updatedUnreadCount[otherUserId] ?: 0) + 1
            }

            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageSenderId" to currentUserId,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp.now(),
                    "unreadCount" to updatedUnreadCount
                )
            ).await()
        }
    }

    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(chatId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Reset unread count for current user
        chatsCollection.document(chatId).update(
            mapOf(
                "unreadCount.$currentUserId" to 0
            )
        ).await()

        // Mark all unread messages from other user as read
        val messages = chatsCollection
            .document(chatId)
            .collection("messages")
            .whereNotEqualTo("senderId", currentUserId)
            .whereEqualTo("read", false)
            .get()
            .await()

        val batch = firestore.batch()
        messages.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        batch.commit().await()
    }

    /**
     * Get total unread message count for current user
     */
    suspend fun getTotalUnreadCount(): Int {
        val currentUserId = auth.currentUser?.uid ?: return 0

        return try {
            val chats = chatsCollection
                .whereArrayContains("participantIds", currentUserId)
                .get()
                .await()
                .toObjects(Chat::class.java)

            chats.sumOf { it.getUnreadCount(currentUserId) }
        } catch (e: Exception) {
            0
        }
    }
}

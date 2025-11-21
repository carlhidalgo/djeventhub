package com.example.djeventhub.ui.chat

import androidx.navigation.NavController
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Helper class to start a chat with another user
 */
object ChatHelper {
    fun startChatWith(
        navController: NavController,
        scope: CoroutineScope,
        otherUserId: String,
        otherUserName: String,
        otherUserImage: String?
    ) {
        scope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val storage = FirebaseStorage.getInstance()
                val userRepository = UserRepository(firestore, auth, storage)
                val chatRepository = ChatRepository(firestore, auth, userRepository)
                val chatId = chatRepository.getOrCreateChat(
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    otherUserImage = otherUserImage
                )
                navController.navigate(Screen.Chat.createRoute(chatId, otherUserName))
            } catch (e: Exception) {
                // Handle error (show snackbar, etc.)
            }
        }
    }
}

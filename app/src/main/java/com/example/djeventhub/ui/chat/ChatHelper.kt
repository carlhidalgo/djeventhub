package com.example.djeventhub.ui.chat

import androidx.navigation.NavController
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.navigation.Screen
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
                val chatRepository = ChatRepository()
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

package com.example.djeventhub.notifications

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Helper class for managing push notifications
 */
object NotificationHelper {

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // No permission needed for Android 12 and below
            true
        }
    }

    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION.SDK_INT) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Get and save FCM token to Firestore
     */
    suspend fun initializeFCM() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            android.util.Log.d("NotificationHelper", "FCM Token: $token")
            
            // Save to Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .await()
                android.util.Log.d("NotificationHelper", "Token saved for user: $userId")
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error getting FCM token", e)
        }
    }

    /**
     * Subscribe to topic for receiving broadcasts
     */
    suspend fun subscribeToTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            android.util.Log.d("NotificationHelper", "Subscribed to topic: $topic")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error subscribing to topic", e)
        }
    }

    /**
     * Unsubscribe from topic
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            android.util.Log.d("NotificationHelper", "Unsubscribed from topic: $topic")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error unsubscribing from topic", e)
        }
    }
}

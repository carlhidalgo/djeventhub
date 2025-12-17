package com.example.djeventhub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.djeventhub.MainActivity
import com.example.djeventhub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Service for handling Firebase Cloud Messaging push notifications
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID_MESSAGES = "messages_channel"
        const val CHANNEL_ID_EVENTS = "events_channel"
        const val CHANNEL_ID_APPLICATIONS = "applications_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Called when a new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "New token: $token")
        
        // Save token to Firestore for the current user
        saveTokenToFirestore(token)
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        android.util.Log.d("FCM", "Message received from: ${message.from}")
        android.util.Log.d("FCM", "Message data: ${message.data}")
        android.util.Log.d("FCM", "Message notification: ${message.notification?.body}")

        // Handle data payload
        message.data.isNotEmpty().let {
            val notificationType = message.data["type"] ?: "general"
            val title = message.data["title"] ?: message.notification?.title ?: "DJEventHub"
            val body = message.data["body"] ?: message.notification?.body ?: ""
            val targetScreen = message.data["targetScreen"]
            val targetId = message.data["targetId"]

            showNotification(notificationType, title, body, targetScreen, targetId)
        }
    }

    /**
     * Create notification channels (required for Android O+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Messages channel (high priority)
            val messagesChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes nuevos en el chat"
                enableVibration(true)
                enableLights(true)
            }

            // Events channel (default priority)
            val eventsChannel = NotificationChannel(
                CHANNEL_ID_EVENTS,
                "Eventos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre eventos próximos y actualizaciones"
            }

            // Applications channel (high priority)
            val applicationsChannel = NotificationChannel(
                CHANNEL_ID_APPLICATIONS,
                "Postulaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre postulaciones a eventos"
                enableVibration(true)
            }

            // General channel (low priority)
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones generales"
            }

            notificationManager.createNotificationChannels(listOf(
                messagesChannel,
                eventsChannel,
                applicationsChannel,
                generalChannel
            ))
        }
    }

    /**
     * Show notification to user
     */
    private fun showNotification(
        type: String,
        title: String,
        body: String,
        targetScreen: String?,
        targetId: String?
    ) {
        val channelId = when (type) {
            "message" -> CHANNEL_ID_MESSAGES
            "event" -> CHANNEL_ID_EVENTS
            "application" -> CHANNEL_ID_APPLICATIONS
            else -> CHANNEL_ID_GENERAL
        }

        // Create intent for notification tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetScreen", targetScreen)
            putExtra("targetId", targetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Save FCM token to Firestore
     */
    private fun saveTokenToFirestore(token: String) {
        serviceScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .await()
                    android.util.Log.d("FCM", "Token saved to Firestore for user: $userId")
                }
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Error saving token to Firestore", e)
            }
        }
    }
}

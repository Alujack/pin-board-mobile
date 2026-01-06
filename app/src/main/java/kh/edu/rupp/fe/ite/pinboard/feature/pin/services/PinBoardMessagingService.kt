package kh.edu.rupp.fe.ite.pinboard.feature.pin.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kh.edu.rupp.fe.ite.pinboard.MainActivity
import kh.edu.rupp.fe.ite.pinboard.R
import kotlin.random.Random

class PinBoardMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("PinBoardMessagingService", "🔄 New FCM token received")
        // Send token to your server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("PinBoardMessagingService", "📨 FCM message received")

        // Handle notification payload
        message.notification?.let { notification ->
            Log.d("PinBoardMessagingService", "📬 Notification payload: ${notification.title}")
            showNotification(
                title = notification.title ?: "PinBoard",
                body = notification.body ?: "",
                data = message.data
            )
        }

        // Handle data payload
        if (message.data.isNotEmpty()) {
            Log.d("PinBoardMessagingService", "📦 Data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        // Check notification permission (required for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("PinBoardMessagingService", "❌ Notification permission not granted, cannot show notification")
                return
            }
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above (if not already created)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "PinBoard Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for likes, comments, and follows"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("PinBoardMessagingService", "✅ Notification channel created")
            }
        }

        // Create intent for notification tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent if needed
            data["pin_id"]?.let { putExtra("pin_id", it) }
            data["board_id"]?.let { putExtra("board_id", it) }
            data["notification_type"]?.let { putExtra("notification_type", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationId = Random.nextInt()
        notificationManager.notify(notificationId, notification)
        Log.d("PinBoardMessagingService", "✅ Notification shown with ID: $notificationId")
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data payload
        val notificationType = data["type"]
        val pinId = data["pin_id"]
        val boardId = data["board_id"]
        val userId = data["user_id"]

        // You can handle different notification types here
        when (notificationType) {
            "PIN_LIKED" -> {
                // Handle pin liked notification
            }
            "PIN_COMMENTED" -> {
                // Handle comment notification
            }
            "NEW_FOLLOWER" -> {
                // Handle new follower notification
            }
            "PIN_SAVED" -> {
                // Handle pin saved notification
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // Store token locally
        val prefs = getSharedPreferences("pinboard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // TODO: Send token to your backend server
        // This should be done when user logs in or when token is refreshed
    }

    companion object {
        private const val CHANNEL_ID = "pinboard_notifications"
    }
}


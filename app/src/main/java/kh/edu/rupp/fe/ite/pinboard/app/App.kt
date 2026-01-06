package kh.edu.rupp.fe.ite.pinboard.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kh.edu.rupp.fe.ite.pinboard.feature.pin.services.FCMTokenManager
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel early
        createNotificationChannel()
        
        // Note: FCM token registration moved to after login
        // See LoginViewModel for FCM token registration after successful authentication
        // Notification permission is requested in MainActivity
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pinboard_notifications",
                "PinBoard Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for likes, comments, and follows"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("App", "✅ Notification channel created in Application")
        }
    }
}

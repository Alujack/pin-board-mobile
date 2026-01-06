package kh.edu.rupp.fe.ite.pinboard.feature.pin.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NotificationPermission"
    }

    /**
     * Check if notification permission is granted
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notification permission is granted by default
            true
        }
    }

    /**
     * Check if we need to request notification permission
     * (Only needed for Android 13+)
     */
    fun shouldRequestPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted()
    }

    /**
     * Log permission status for debugging
     */
    fun logPermissionStatus() {
        val granted = isNotificationPermissionGranted()
        val shouldRequest = shouldRequestPermission()
        Log.d(TAG, "Notification Permission Status:")
        Log.d(TAG, "  - SDK Version: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "  - Permission Granted: $granted")
        Log.d(TAG, "  - Should Request: $shouldRequest")
    }
}


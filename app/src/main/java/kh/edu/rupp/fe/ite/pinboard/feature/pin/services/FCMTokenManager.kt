package kh.edu.rupp.fe.ite.pinboard.feature.pin.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PinRepository
) {
    private val prefs = context.getSharedPreferences("pinboard_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initializeFCM() {
        scope.launch {
            try {
                Log.d("FCMTokenManager", "=== Starting FCM Token Registration ===")
                
                // Get FCM token
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCMTokenManager", "✅ FCM Token obtained: ${token.take(20)}...")

                // Always register token with backend on login/initialization
                // This ensures token is registered even if it hasn't changed
                Log.d("FCMTokenManager", "📤 Registering FCM token with backend...")
                when (val result = repository.registerFCMToken(token)) {
                    is PinResult.Success -> {
                        // Save token locally
                        prefs.edit().putString("fcm_token", token).apply()
                        Log.d("FCMTokenManager", "✅ FCM token registered successfully with backend")
                    }
                    is PinResult.Error -> {
                        Log.e("FCMTokenManager", "❌ Failed to register FCM token: ${result.message}")
                        // Still save token locally for retry later
                        prefs.edit().putString("fcm_token", token).apply()
                    }
                }
                
                Log.d("FCMTokenManager", "=== FCM Token Registration Complete ===")
            } catch (e: Exception) {
                Log.e("FCMTokenManager", "❌ Failed to get FCM token", e)
                e.printStackTrace()
            }
        }
    }

    fun refreshToken() {
        scope.launch {
            try {
                FirebaseMessaging.getInstance().deleteToken().await()
                initializeFCM()
            } catch (e: Exception) {
                Log.e("FCMTokenManager", "Failed to refresh FCM token", e)
            }
        }
    }

    /**
     * Force register FCM token (useful for retry or manual registration)
     */
    fun forceRegisterFCMToken() {
        scope.launch {
            try {
                Log.d("FCMTokenManager", "🔄 Force registering FCM token...")
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCMTokenManager", "✅ FCM Token obtained: ${token.take(20)}...")
                
                when (val result = repository.registerFCMToken(token)) {
                    is PinResult.Success -> {
                        prefs.edit().putString("fcm_token", token).apply()
                        Log.d("FCMTokenManager", "✅ FCM token force registered successfully")
                    }
                    is PinResult.Error -> {
                        Log.e("FCMTokenManager", "❌ Failed to force register FCM token: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FCMTokenManager", "❌ Failed to force register FCM token", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Remove FCM token from backend and local storage (call on logout)
     */
    fun removeFCMToken() {
        scope.launch {
            try {
                // Remove from backend
                when (val result = repository.removeFCMToken()) {
                    is PinResult.Success -> {
                        Log.d("FCMTokenManager", "FCM token removed from backend successfully")
                    }
                    is PinResult.Error -> {
                        Log.e("FCMTokenManager", "Failed to remove FCM token from backend: ${result.message}")
                    }
                }
                
                // Remove from local storage
                prefs.edit().remove("fcm_token").apply()
                Log.d("FCMTokenManager", "FCM token removed from local storage")
            } catch (e: Exception) {
                Log.e("FCMTokenManager", "Failed to remove FCM token", e)
            }
        }
    }
}


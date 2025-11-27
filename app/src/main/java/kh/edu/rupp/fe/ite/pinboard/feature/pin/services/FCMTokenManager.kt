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
                // Get FCM token
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCMTokenManager", "FCM Token: $token")

                // Check if token has changed
                val savedToken = prefs.getString("fcm_token", null)
                if (token != savedToken) {
                    // Register token with backend
                    when (val result = repository.registerFCMToken(token)) {
                        is PinResult.Success -> {
                            // Save token locally
                            prefs.edit().putString("fcm_token", token).apply()
                            Log.d("FCMTokenManager", "FCM token registered successfully")
                        }
                        is PinResult.Error -> {
                            Log.e("FCMTokenManager", "Failed to register FCM token: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FCMTokenManager", "Failed to get FCM token", e)
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
}


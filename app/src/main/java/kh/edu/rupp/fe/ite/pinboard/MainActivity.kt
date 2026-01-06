package kh.edu.rupp.fe.ite.pinboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kh.edu.rupp.fe.ite.pinboard.app.navigation.AuthNavGraph
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.pin.services.FCMTokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.pin.services.NotificationPermissionManager
import kh.edu.rupp.fe.ite.pinboard.ui.theme.PinboardTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenManager: TokenManager
    
    @Inject
    lateinit var fcmTokenManager: FCMTokenManager
    
    @Inject
    lateinit var notificationPermissionManager: NotificationPermissionManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Notification permission granted")
            // Initialize FCM after permission is granted
            initializeFCMIfLoggedIn()
        } else {
            Log.w("MainActivity", "❌ Notification permission denied")
            // Still try to initialize FCM (it might work on older Android versions)
            // But notifications won't show on Android 13+
            initializeFCMIfLoggedIn()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Create notification channel early
        createNotificationChannel()
        
        // Request notification permission if needed (Android 13+)
        requestNotificationPermissionIfNeeded()

        setContent {
            PinboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AuthNavGraph(
                        navController = navController,
                        tokenManager = tokenManager,
                        fcmTokenManager = fcmTokenManager
                    )
                }
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "pinboard_notifications",
                "PinBoard Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for likes, comments, and follows"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("MainActivity", "✅ Notification channel created")
        }
    }
    
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("MainActivity", "✅ Notification permission already granted")
                    initializeFCMIfLoggedIn()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // User previously denied, show rationale if needed
                    Log.d("MainActivity", "📱 Requesting notification permission (previously denied)")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // First time requesting
                    Log.d("MainActivity", "📱 Requesting notification permission (first time)")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below - permission granted by default
            Log.d("MainActivity", "✅ Android 12 or below - notification permission granted by default")
            initializeFCMIfLoggedIn()
        }
    }
    
    private fun initializeFCMIfLoggedIn() {
        lifecycleScope.launch {
            val token = tokenManager.getToken()
            if (!token.isNullOrEmpty()) {
                // User is already logged in, register FCM token
                // Use a small delay to ensure auth context is ready
                kotlinx.coroutines.delay(500)
                fcmTokenManager.initializeFCM()
            }
        }
    }
}

package kh.edu.rupp.fe.ite.pinboard.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kh.edu.rupp.fe.ite.pinboard.feature.pin.services.FCMTokenManager
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    override fun onCreate() {
        super.onCreate()
        // Initialize FCM token registration
        fcmTokenManager.initializeFCM()
    }
}

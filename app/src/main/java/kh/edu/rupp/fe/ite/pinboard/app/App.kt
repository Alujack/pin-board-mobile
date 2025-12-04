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
        // Note: FCM token registration moved to after login
        // See LoginViewModel for FCM token registration after successful authentication
    }
}

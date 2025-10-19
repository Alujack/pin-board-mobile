package kh.edu.rupp.fe.ite.pinboard.feature.notification.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

class NotificationPrefs(private val context: Context) {
    companion object {
        private val KEY_OPT_IN_DONE = booleanPreferencesKey("notifications_opt_in_done")
    }

    val optInDone: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[KEY_OPT_IN_DONE] ?: false
    }

    suspend fun setOptInDone(done: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[KEY_OPT_IN_DONE] = done
        }
    }
}

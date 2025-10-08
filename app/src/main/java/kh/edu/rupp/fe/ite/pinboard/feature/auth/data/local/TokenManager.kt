// TokenManager.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val SESSION_ID_KEY = stringPreferencesKey("session_id")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.map { it[TOKEN_KEY] }.first()
        return prefs
    }

    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_ID_KEY] = sessionId
        }
    }

    suspend fun getSessionId(): String? {
        val prefs = context.dataStore.data.map { it[SESSION_ID_KEY] }.first()
        return prefs
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun clearSessionId() {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_ID_KEY)
        }
    }

    suspend fun clearAllTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(SESSION_ID_KEY)
        }
    }

    fun getTokenSync(): String? {
        return runBlocking { getToken() }
    }

    fun getSessionIdSync(): String? {
        return runBlocking { getSessionId() }
    }
}

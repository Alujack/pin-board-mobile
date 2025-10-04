package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If we already tried to refresh, give up
        if (response.request.header("Authorization") != null &&
            response.priorResponse?.code == 401) {
            return null
        }

        val refreshToken = runBlocking {
            tokenManager.refreshToken.first()
        } ?: return null

        // Try to refresh the token
        val newTokens = runBlocking {
            try {
                authApi.refreshToken(RefreshTokenRequest(refreshToken))
            } catch (e: Exception) {
                null
            }
        } ?: return null

        // Save new tokens
        runBlocking {
            tokenManager.saveTokens(newTokens.token, newTokens.refreshToken)
        }

        // Retry the request with new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.token}")
            .build()
    }
}
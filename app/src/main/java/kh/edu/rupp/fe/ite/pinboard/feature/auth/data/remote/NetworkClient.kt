package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote

import android.util.Log
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kh.edu.rupp.fe.ite.pinboard.BuildConfig

@Singleton
class NetworkClient @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Avoid logging large multipart bodies to prevent OOM
            level = HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(RefreshTokenInterceptor())
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

    /**
     * Auth interceptor that adds the access token to requests
     */
    private inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val newRequest = request.newBuilder()

            // Skip auth for login, register, and refresh endpoints
            if (shouldSkipAuth(request.url.encodedPath)) {
                return chain.proceed(newRequest.build())
            }

            // Add access token
            val accessToken = tokenManager.getTokenSync()
            accessToken?.let { token ->
                newRequest.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(newRequest.build())
        }

        private fun shouldSkipAuth(path: String): Boolean {
            return path.contains("/api/auth/login") ||
                    path.contains("/api/auth/register") ||
                    path.contains("/api/auth/refresh")
        }
    }
    private inner class RefreshTokenInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            // Proceed with the original request
            val response = chain.proceed(request)

            // If we get 401, it might be an expired token
            if (response.code == 401) {
                // Use runBlocking to call suspend functions from this synchronous interceptor
                return runBlocking {
                    val sessionId = tokenManager.getSessionIdSync() // Assuming this is still synchronous for now
                    if (sessionId == null) {
                        // No session ID, clear tokens and return the original 401 response
                        tokenManager.clearAllTokens()
                        return@runBlocking response
                    }

                    // Create a temporary Retrofit instance for the refresh call
                    val tempRetrofit = Retrofit.Builder()
                        .baseUrl(BuildConfig.API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpClient()) // Use a basic client for the refresh call
                        .build()

                    val authApi = tempRetrofit.create(AuthApi::class.java)

                    try {
                        // This is the suspend call, now safely wrapped in a coroutine
                        val refreshResponse = authApi.refreshToken(RefreshTokenRequest(sessionId))

                        if (refreshResponse.isSuccessful) {
                            val refreshData = refreshResponse.body()
                            if (refreshData != null) {
                                // Close the original failed response before proceeding
                                response.close()

                                // Save new tokens
                                tokenManager.saveToken(refreshData.sessionToken)
                                tokenManager.saveSessionId(refreshData.sessionId)

                                // Retry the original request with the new token
                                val newRequest = request.newBuilder()
                                    .removeHeader("Authorization")
                                    .addHeader("Authorization", "Bearer ${refreshData.sessionToken}")
                                    .build()

                                // Proceed with the new request
                                return@runBlocking chain.proceed(newRequest)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NetworkClient", "Token refresh failed", e)
                        // If refresh fails for any reason, clear tokens
                        tokenManager.clearAllTokens()
                    }

                    // If token refresh fails, return the original 401 response
                    return@runBlocking response
                }
            }

            // For all other cases, return the original response
            return response
        }
    }
//
//    private fun shouldSkipAuth(path: String): Boolean {
//            return path.contains("/api/auth/login") ||
//                    path.contains("/api/auth/register") ||
//                    path.contains("/api/auth/refresh")
//        }
//    }
}

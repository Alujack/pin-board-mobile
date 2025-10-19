package kh.edu.rupp.fe.ite.pinboard.core.network

import android.util.Log
import kh.edu.rupp.fe.ite.pinboard.BuildConfig
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
            // Match user's original working style
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

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    /**
     * Auth interceptor that adds the access token to requests
     */
    private inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val newRequest = request.newBuilder()

            if (shouldSkipAuth(request.url.encodedPath)) {
                return chain.proceed(newRequest.build())
            }

            val accessToken = tokenManager.getTokenSync()
            accessToken?.let { token ->
                newRequest.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(newRequest.build())
        }

        private fun shouldSkipAuth(path: String): Boolean {
            // Use user's previous logic: only skip auth endpoints
            return path.contains("/api/auth/login") ||
                path.contains("/api/auth/register") ||
                path.contains("/api/auth/refresh")
        }
    }

    private inner class RefreshTokenInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            if (response.code == 401) {
                return runBlocking {
                    val sessionId = tokenManager.getSessionIdSync()
                    if (sessionId == null) {
                        tokenManager.clearAllTokens()
                        return@runBlocking response
                    }

                    val tempRetrofit = Retrofit.Builder()
                        .baseUrl(BuildConfig.API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpClient())
                        .build()

                    val authApi = tempRetrofit.create(
                        kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi::class.java
                    )

                    try {
                        val refreshResponse = authApi.refreshToken(
                            kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.RefreshTokenRequest(sessionId)
                        )

                        if (refreshResponse.isSuccessful) {
                            val refreshData = refreshResponse.body()
                            if (refreshData != null) {
                                response.close()
                                tokenManager.saveToken(refreshData.sessionToken)
                                tokenManager.saveSessionId(refreshData.sessionId)

                                val newRequest = request.newBuilder()
                                    .removeHeader("Authorization")
                                    .addHeader(
                                        "Authorization",
                                        "Bearer ${refreshData.sessionToken}"
                                    )
                                    .build()

                                return@runBlocking chain.proceed(newRequest)
                            }
                        } else {
                            tokenManager.clearAllTokens()
                        }
                    } catch (e: Exception) {
                        Log.e("NetworkClient", "Token refresh failed", e)
                        tokenManager.clearAllTokens()
                    }

                    return@runBlocking response
                }
            }

            return response
        }
    }
}

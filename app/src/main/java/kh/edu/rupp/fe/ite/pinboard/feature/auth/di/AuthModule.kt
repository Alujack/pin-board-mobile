// feature/auth/di/AuthModule.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.NetworkClient
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository.AuthRepositoryImpl
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kh.edu.rupp.fe.ite.pinboard.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val newRequest = request.newBuilder()

                    // Add token to requests if available
                    val token = tokenManager.getTokenSync()
                    token?.let { newRequest.addHeader("Authorization", "Bearer $token") }

                    chain.proceed(newRequest.build())
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkClient(tokenManager: TokenManager): NetworkClient {
        return NetworkClient(tokenManager)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApi, tokenManager: TokenManager): AuthRepository {
        return AuthRepositoryImpl(authApi, tokenManager)
    }
}

// feature/auth/di/AuthModule.kt
package kh.edu.rupp.fe.ite.pinboard.feature.auth.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.TokenManager
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.AuthApi
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.repository.AuthRepositoryImpl
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.repository.AuthRepository
import kh.edu.rupp.fe.ite.pinboard.core.network.NetworkClient

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthApi(networkClient: NetworkClient): AuthApi {
        return networkClient.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApi, tokenManager: TokenManager): AuthRepository {
        return AuthRepositoryImpl(authApi, tokenManager)
    }
}

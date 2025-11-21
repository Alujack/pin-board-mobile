package kh.edu.rupp.fe.ite.pinboard.feature.pin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.NetworkClient
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePinApi(networkClient: NetworkClient): PinApi =
        networkClient.create(PinApi::class.java)

    @Provides
    @Singleton
    fun provideCommentApi(networkClient: NetworkClient): CommentApi =
        networkClient.create(CommentApi::class.java)

    @Provides
    @Singleton
    fun providePinLikeApi(networkClient: NetworkClient): PinLikeApi =
        networkClient.create(PinLikeApi::class.java)

    @Provides
    @Singleton
    fun provideShareApi(networkClient: NetworkClient): ShareApi =
        networkClient.create(ShareApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(networkClient: NetworkClient): NotificationApi =
        networkClient.create(NotificationApi::class.java)
}

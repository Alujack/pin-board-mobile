package kh.edu.rupp.fe.ite.pinboard.feature.pin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.NetworkClient
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePinApi(networkClient: NetworkClient): PinApi =
        networkClient.create(PinApi::class.java)
}

package kh.edu.rupp.fe.ite.pinboard.feature.pin.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.repository.PinRepositoryImpl
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.NetworkClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PinModule {

    @Provides
    @Singleton
    fun providePinApi(networkClient: NetworkClient): PinApi {
        return networkClient.create(PinApi::class.java)
    }

    @Provides
    @Singleton
    fun providePinRepository(pinApi: PinApi): PinRepository {
        return PinRepositoryImpl(pinApi)
    }
}

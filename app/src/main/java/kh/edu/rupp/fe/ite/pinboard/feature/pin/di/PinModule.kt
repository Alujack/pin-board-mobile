package kh.edu.rupp.fe.ite.pinboard.feature.pin.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.PinRepositoryImpl
import kh.edu.rupp.fe.ite.pinboard.feature.pin.data.remote.PinApi
import kh.edu.rupp.fe.ite.pinboard.feature.pin.domain.repository.PinRepository
<<<<<<< HEAD
import kh.edu.rupp.fe.ite.pinboard.core.network.NetworkClient
=======
import retrofit2.Retrofit
>>>>>>> 40a60d3cb3ec2585ec91588424bb3f6b59a375a1
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PinBindModule {
    @Binds
    @Singleton
    abstract fun bindPinRepository(impl: PinRepositoryImpl): PinRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PinProvideModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext ctx: Context): Context = ctx
}
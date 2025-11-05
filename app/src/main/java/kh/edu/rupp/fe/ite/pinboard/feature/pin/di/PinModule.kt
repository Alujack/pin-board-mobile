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
import retrofit2.Retrofit
=======
import kh.edu.rupp.fe.ite.pinboard.core.network.NetworkClient
>>>>>>> 3fd45835a414839e5d7266d3a8bae96a231c7688
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
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
import retrofit2.Retrofit
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
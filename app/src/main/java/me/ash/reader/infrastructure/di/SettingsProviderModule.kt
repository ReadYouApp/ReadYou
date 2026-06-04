package me.ash.reader.infrastructure.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.infrastructure.preference.SettingsProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsProviderModule {
    @Provides
    @Singleton
    fun provideSettingsProvider(
        @ApplicationContext context: Context,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope applicationScope: CoroutineScope
    ): SettingsProvider = SettingsProvider(
        context = context, coroutineScope = applicationScope, ioDispatcher = ioDispatcher
    )
}
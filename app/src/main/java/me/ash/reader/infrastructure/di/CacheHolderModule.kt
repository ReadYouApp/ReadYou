package me.ash.reader.infrastructure.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import me.ash.reader.infrastructure.rss.RssHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheHolderModule {
    @Provides
    @Singleton
    fun provideDiffMapHolder(
        @ApplicationContext context: Context,
        @ApplicationScope applicationScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        accountService: AccountService,
        rssService: RssService,
    ): DiffMapHolder {
        return DiffMapHolder(
            context = context, applicationScope, ioDispatcher, accountService, rssService
        )
    }

    @Provides
    @Singleton
    fun provideCacheHelper(
        @ApplicationContext context: Context,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        rssHelper: RssHelper,
        accountService: AccountService,
    ): ReaderCacheHelper = ReaderCacheHelper(
        context = context, ioDispatcher = ioDispatcher,
        rssHelper = rssHelper,
        accountService = accountService,
    )
}
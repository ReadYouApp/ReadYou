package me.ash.reader.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.preference.SettingsProvider

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun providesArticlePagingList(
        rssService: RssService,
        androidStringsHelper: AndroidStringsHelper,
        @ApplicationScope applicationScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        settingsProvider: SettingsProvider,
        filterStateUseCase: FilterStateUseCase,
        accountService: AccountService,
    ): ArticlePagingListUseCase {
        return ArticlePagingListUseCase(
            rssService,
            androidStringsHelper,
            applicationScope,
            ioDispatcher,
            settingsProvider,
            filterStateUseCase,
            accountService,
        )
    }

    @Provides
    @Singleton
    fun providesGroupWithFeedsList(
        @ApplicationScope applicationScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        settingsProvider: SettingsProvider,
        rssService: RssService,
        filterStateUseCase: FilterStateUseCase,
        diffMapHolder: DiffMapHolder,
        accountService: AccountService,
    ): GroupWithFeedsListUseCase {
        return GroupWithFeedsListUseCase(
            applicationScope,
            ioDispatcher,
            settingsProvider,
            rssService,
            filterStateUseCase,
            diffMapHolder,
            accountService,
        )
    }
}

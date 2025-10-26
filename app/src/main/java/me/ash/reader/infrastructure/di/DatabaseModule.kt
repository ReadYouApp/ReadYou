package me.ash.reader.infrastructure.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.domain.repository.KeywordFilterDao
import me.ash.reader.infrastructure.db.AndroidDatabase
import javax.inject.Singleton

/**
 * Provides Data Access Objects for database.
 *
 * - [ArticleDao]
 * - [FeedDao]
 * - [GroupDao]
 * - [KeywordFilterDao]
 * - [AccountDao]
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticleDao(androidDatabase: AndroidDatabase): ArticleDao =
        androidDatabase.articleDao()

    @Provides
    @Singleton
    fun provideFeedDao(androidDatabase: AndroidDatabase): FeedDao =
        androidDatabase.feedDao()

    @Provides
    @Singleton
    fun provideGroupDao(androidDatabase: AndroidDatabase): GroupDao =
        androidDatabase.groupDao()

    @Provides
    @Singleton
    fun provideKeywordFilterDao(androidDatabase: AndroidDatabase): KeywordFilterDao =
        androidDatabase.keywordFilterDao()

    @Provides
    @Singleton
    fun provideAccountDao(androidDatabase: AndroidDatabase): AccountDao =
        androidDatabase.accountDao()

    @Provides
    @Singleton
    fun provideReaderDatabase(@ApplicationContext context: Context): AndroidDatabase =
        AndroidDatabase.getInstance(context)
}

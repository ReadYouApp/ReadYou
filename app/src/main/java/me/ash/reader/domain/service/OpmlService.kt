package me.ash.reader.domain.service

import android.content.Context
import be.ceau.opml.OpmlWriter
import be.ceau.opml.entity.Body
import be.ceau.opml.entity.Head
import be.ceau.opml.entity.Opml
import be.ceau.opml.entity.Outline
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.domain.repository.KeywordFilterDao
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.OPMLDataSource
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.ext.getDefaultGroupId
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Supports import and export from OPML files.
 */
class OpmlService @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val keywordFilterDao: KeywordFilterDao,
    private val accountService: AccountService,
    private val rssService: RssService,
    private val OPMLDataSource: OPMLDataSource,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * Imports OPML file.
     *
     * @param [inputStream] input stream of OPML file
     */
    @Throws(Exception::class)
    suspend fun saveToDatabase(inputStream: InputStream) {
        withContext(ioDispatcher) {
            val defaultGroup = groupDao.queryById(getDefaultGroupId(context.currentAccountId))!!
            val groupWithFeedsWithKeywordFiltersList =
                OPMLDataSource.parseFileInputStream(inputStream, defaultGroup, context.currentAccountId)
            groupWithFeedsWithKeywordFiltersList.forEach { groupWithFeedsWithKeywordFilters ->
                if (groupWithFeedsWithKeywordFilters.group != defaultGroup) {
                    groupDao.insert(groupWithFeedsWithKeywordFilters.group)
                }
                val repeatList = mutableListOf<Feed>()
                groupWithFeedsWithKeywordFilters.feedsWithKeywordFilters.forEach {
                    it.feed.groupId = groupWithFeedsWithKeywordFilters.group.id
                    if (rssService.get().isFeedExist(it.feed.url)) {
                        repeatList.add(it.feed)
                    }
                }
                feedDao.insertList((groupWithFeedsWithKeywordFilters.feedsWithKeywordFilters.map { it.feed } subtract repeatList.toSet()).toList())
                keywordFilterDao.insertList(groupWithFeedsWithKeywordFilters.feedsWithKeywordFilters.map { it.keywordFilters }
                    .flatten())
            }
        }
    }

    /**
     * Exports OPML file.
     */
    @Throws(Exception::class)
    suspend fun saveToString(accountId: Int, attachInfo: Boolean): String {
        val defaultGroup = groupDao.queryById(getDefaultGroupId(accountId))
        val allFilteredKeywords = keywordFilterDao.queryAllBlocking()
        return OpmlWriter().write(
            Opml(
                "2.0",
                Head(
                    accountService.getCurrentAccount().name,
                    Date().toString(), null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                ),
                Body(groupDao.queryAllGroupWithFeed(accountId).map {
                    Outline(
                        mutableMapOf(
                            "text" to it.group.name,
                            "title" to it.group.name,
                        ).apply {
                            if (attachInfo) {
                                put("isDefault", (it.group.id == defaultGroup?.id).toString())
                            }
                        },
                        it.feeds.map { feed ->
                            Outline(
                                mutableMapOf(
                                    "text" to feed.name,
                                    "title" to feed.name,
                                    "xmlUrl" to feed.url,
                                    "htmlUrl" to feed.url
                                ).apply {
                                    if (attachInfo) {
                                        put("isNotification", feed.isNotification.toString())
                                        put("isFullContent", feed.isFullContent.toString())
                                        put("isBrowser", feed.isBrowser.toString())
                                        put(
                                            "filteredKeywords",
                                            Json.encodeToString(allFilteredKeywords.filter { keyword -> keyword.feedId == feed.id }
                                                .map { keyword -> keyword.keyword })
                                        )
                                    }
                                },
                                listOf()
                            )
                        }
                    )
                })
            )
        )!!
    }

    private fun getDefaultGroupId(accountId: Int): String = accountId.getDefaultGroupId()
}

package me.ash.reader.domain.service

import android.util.Log
import androidx.paging.PagingSource
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rometools.rome.feed.synd.SyndFeed
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.article.ArchivedArticle
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.preference.KeepArchivedPreference
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.spacerDollar

abstract class AbstractRssRepository(
    private val articleDao: ArticleDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val workManager: WorkManager,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val dispatcherIO: CoroutineDispatcher,
    private val dispatcherDefault: CoroutineDispatcher,
    private val accountService: AccountService,
) {

    open val importSubscription: Boolean = true
    open val addSubscription: Boolean = true
    open val moveSubscription: Boolean = true
    open val deleteSubscription: Boolean = true
    open val updateSubscription: Boolean = true

    open suspend fun validCredentials(account: Account): Boolean = true

    open suspend fun clearAuthorization() {}

    open suspend fun subscribe(
        feedLink: String,
        searchedFeed: SyndFeed,
        groupId: String,
        isNotification: Boolean,
        isFullContent: Boolean,
        isBrowser: Boolean,
    ) {
        val accountId = accountService.getCurrentAccountId()
        val feed =
            Feed(
                id = accountId.spacerDollar(UUID.randomUUID().toString()),
                name = searchedFeed.title.decodeHTML()!!,
                url = feedLink,
                groupId = groupId,
                accountId = accountId,
                icon = searchedFeed.icon?.link,
                isBrowser = isBrowser,
                isNotification = isNotification,
                isFullContent = isFullContent,
            )
        val articles =
            searchedFeed.entries.map { rssHelper.buildArticleFromSyndEntry(feed, accountId, it) }
        feedDao.insert(feed)
        articleDao.insertList(articles.map { it.copy(feedId = feed.id) })
    }

    open suspend fun addGroup(destFeed: Feed?, newGroupName: String): String {
        accountService.getCurrentAccountId().let { accountId ->
            return accountId.spacerDollar(UUID.randomUUID().toString()).also {
                groupDao.insert(Group(id = it, name = newGroupName, accountId = accountId))
            }
        }
    }

    abstract suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?,
    ): ListenableWorker.Result

    open suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        val accountId = accountService.getCurrentAccountId()
        when {
            groupId != null -> {
                articleDao.markAllAsReadByGroupId(
                    accountId = accountId,
                    groupId = groupId,
                    isUnread = isUnread,
                    before = before ?: Date(Long.MAX_VALUE),
                )
            }

            feedId != null -> {
                articleDao.markAllAsReadByFeedId(
                    accountId = accountId,
                    feedId = feedId,
                    isUnread = isUnread,
                    before = before ?: Date(Long.MAX_VALUE),
                )
            }

            articleId != null -> {
                articleDao.markAsReadByArticleId(accountId, articleId, isUnread)
            }

            else -> {
                articleDao.markAllAsRead(accountId, isUnread, before ?: Date(Long.MAX_VALUE))
            }
        }
    }

    open suspend fun batchMarkAsRead(articleIds: Set<String>, isUnread: Boolean) {
        val accountId = accountService.getCurrentAccountId()
        articleIds
            .takeIf { it.isNotEmpty() }
            ?.chunked(500)
            ?.forEachIndexed { index, it ->
                articleDao.markAsReadByIdSet(accountId, it.toSet(), isUnread)
            }
    }

    open suspend fun syncReadStatus(articleIds: Set<String>, isUnread: Boolean): Set<String> {
        /* no-op */
        return emptySet()
    }

    open suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        val accountId = accountService.getCurrentAccountId()
        articleDao.markAsStarredByArticleId(accountId, articleId, isStarred)
    }

    suspend fun clearKeepArchivedArticles(): List<Article> {
        val accountId = accountService.getCurrentAccountId()
        val currentAccount = accountService.getCurrentAccount()
        val keepArchived = currentAccount.keepArchived
        if (keepArchived != KeepArchivedPreference.Always) {
            val archivedArticles =
                articleDao.queryArchivedArticleBefore(
                    accountId,
                    Date(System.currentTimeMillis() - keepArchived.value),
                )
            articleDao.delete(*archivedArticles.toTypedArray())
            return archivedArticles.also {
                feedDao.insertArchivedArticles(
                    it.map { ArchivedArticle(feedId = it.feedId, link = it.link) }
                )
            }
        }
        return emptyList()
    }

    fun cancelSync() {
        SyncWorker.cancelPeriodicWork(workManager)
        SyncWorker.cancelOneTimeWork(workManager)
    }

    open fun doSyncOneTime(
        accountId: Int = accountService.getCurrentAccountId(),
        feedId: String? = null,
        groupId: String? = null,
    ) {
        SyncWorker.enqueueOneTimeWork(
            workManager,
            workDataOf("accountId" to accountId, "feedId" to feedId, "groupId" to groupId),
        )
    }

    fun initSync() {
        accountService.getCurrentAccount().let {
            val syncOnStart = it.syncOnStart.value
            if (syncOnStart) {
                doSyncOneTime(it.id!!)
            }
            if (it.syncInterval.value != SyncIntervalPreference.Manually.value) {
                SyncWorker.enqueuePeriodicWork(account = it, workManager = workManager)
                WidgetUpdateWorker.enqueuePeriodicWork(
                    workManager = workManager,
                    syncInterval = it.syncInterval,
                    syncOnlyWhenCharging = it.syncOnlyWhenCharging,
                    syncOnlyOnWiFi = it.syncOnlyOnWiFi,
                )
            } else {
                SyncWorker.cancelPeriodicWork(workManager)
                WidgetUpdateWorker.cancelPeriodicWork(workManager)
            }
        }
    }

    fun pullGroups(): Flow<MutableList<Group>> =
        groupDao.queryAllGroup(accountService.getCurrentAccountId()).flowOn(dispatcherIO)

    fun pullFeeds(): Flow<MutableList<GroupWithFeed>> =
        groupDao
            .queryAllGroupWithFeedAsFlow(accountService.getCurrentAccountId())
            .flowOn(dispatcherIO)

    fun pullArticles(
        groupId: String?,
        feedId: String?,
        isStarred: Boolean,
        isUnread: Boolean,
        sortAscending: Boolean = false,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = accountService.getCurrentAccountId()
        Log.i(
            "RLog",
            "pullArticles: accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}",
        )
        return when {
            groupId != null ->
                when {
                    isStarred ->
                        articleDao.queryArticleWithFeedByGroupIdWhenIsStarred(
                            accountId,
                            groupId,
                            true,
                        )

                    isUnread ->
                        articleDao.queryArticleWithFeedByGroupIdWhenIsUnread(
                            accountId,
                            groupId,
                            true,
                            sortAscending = sortAscending,
                        )

                    else -> articleDao.queryArticleWithFeedByGroupIdWhenIsAll(accountId, groupId)
                }

            feedId != null ->
                when {
                    isStarred ->
                        articleDao.queryArticleWithFeedByFeedIdWhenIsStarred(
                            accountId,
                            feedId,
                            true,
                        )

                    isUnread ->
                        articleDao.queryArticleWithFeedByFeedIdWhenIsUnread(
                            accountId,
                            feedId,
                            true,
                            sortAscending = sortAscending,
                        )

                    else -> articleDao.queryArticleWithFeedByFeedIdWhenIsAll(accountId, feedId)
                }

            else ->
                when {
                    isStarred -> articleDao.queryArticleWithFeedWhenIsStarred(accountId, true)
                    isUnread ->
                        articleDao.queryArticleWithFeedWhenIsUnread(
                            accountId,
                            true,
                            sortAscending = sortAscending,
                        )

                    else -> articleDao.queryArticleWithFeedWhenIsAll(accountId)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun pullImportant(isStarred: Boolean, isUnread: Boolean): Flow<Map<String, Int>> {
        val accountId = accountService.getCurrentAccountId()
        Log.i(
            "RLog",
            "pullImportant: accountId: ${accountId}, isStarred: ${isStarred}, isUnread: ${isUnread}",
        )
        return when {
            isStarred -> articleDao.queryImportantCountWhenIsStarred(accountId, true)
            isUnread -> articleDao.queryImportantCountWhenIsUnread(accountId, true)
            else -> articleDao.queryImportantCountWhenIsAll(accountId)
        }
    }

    suspend fun findFeedById(id: String): Feed? = feedDao.queryById(id)

    suspend fun findGroupById(id: String): Group? = groupDao.queryById(id)

    suspend fun findArticleById(id: String): ArticleWithFeed? = articleDao.queryById(id)

    suspend fun isFeedExist(url: String): Boolean =
        feedDao.queryByLink(accountService.getCurrentAccountId(), url).isNotEmpty()

    suspend fun queryAllGroupWithFeeds(): List<GroupWithFeed> =
        groupDao.queryAllGroupWithFeed(accountService.getCurrentAccountId())

    open suspend fun renameGroup(group: Group) {
        groupDao.update(group)
    }

    open suspend fun renameFeed(feed: Feed) {
        updateFeed(feed)
    }

    open suspend fun moveFeed(originGroupId: String, feed: Feed) {
        updateFeed(feed)
    }

    open suspend fun changeFeedUrl(feed: Feed) {
        updateFeed(feed)
    }

    internal suspend fun updateFeed(feed: Feed) {
        feedDao.update(feed)
    }

    open suspend fun deleteGroup(group: Group, onlyDeleteNoStarred: Boolean? = false) {
        val accountId = accountService.getCurrentAccountId()
        if (
            onlyDeleteNoStarred == true &&
                articleDao.countByGroupIdWhenIsStarred(accountId, group.id, true) > 0
        ) {
            return
        }
        deleteArticles(group = group, includeStarred = true)
        feedDao.deleteByGroupId(accountId, group.id)
        groupDao.delete(group)
    }

    open suspend fun deleteFeed(feed: Feed, onlyDeleteNoStarred: Boolean? = false) {
        if (
            onlyDeleteNoStarred == true &&
                articleDao.countByFeedIdWhenIsStarred(
                    accountService.getCurrentAccountId(),
                    feed.id,
                    true,
                ) > 0
        ) {
            return
        }
        deleteArticles(feed = feed, includeStarred = true)
        feedDao.delete(feed)
    }

    suspend fun deleteArticles(
        group: Group? = null,
        feed: Feed? = null,
        includeStarred: Boolean = false,
    ) {
        when {
            group != null ->
                articleDao.deleteByGroupId(
                    accountService.getCurrentAccountId(),
                    group.id,
                    includeStarred,
                )

            feed != null ->
                articleDao.deleteByFeedId(
                    accountService.getCurrentAccountId(),
                    feed.id,
                    includeStarred,
                )
        }
    }

    suspend fun deleteAccountArticles(accountId: Int) {
        articleDao.deleteByAccountId(accountId)
    }

    suspend fun groupParseFullContent(group: Group, isFullContent: Boolean) {
        feedDao.updateIsFullContentByGroupId(
            accountService.getCurrentAccountId(),
            group.id,
            isFullContent,
        )
    }

    suspend fun groupOpenInBrowser(group: Group, isBrowser: Boolean) {
        feedDao.updateIsBrowserByGroupId(accountService.getCurrentAccountId(), group.id, isBrowser)
    }

    suspend fun groupAllowNotification(group: Group, isNotification: Boolean) {
        feedDao.updateIsNotificationByGroupId(
            accountService.getCurrentAccountId(),
            group.id,
            isNotification,
        )
    }

    suspend fun groupMoveToTargetGroup(group: Group, targetGroup: Group) {
        feedDao.updateTargetGroupIdByGroupId(
            accountService.getCurrentAccountId(),
            group.id,
            targetGroup.id,
        )
    }

    fun searchArticles(
        content: String,
        groupId: String?,
        feedId: String?,
        isStarred: Boolean,
        isUnread: Boolean,
        sortAscending: Boolean = false,
    ): PagingSource<Int, ArticleWithFeed> {
        val accountId = accountService.getCurrentAccountId()
        Log.i(
            "RLog",
            "searchArticles: content: ${content}, accountId: ${accountId}, groupId: ${groupId}, feedId: ${feedId}, isStarred: ${isStarred}, isUnread: ${isUnread}",
        )
        return when {
            groupId != null ->
                when {
                    isStarred ->
                        articleDao.searchArticleByGroupIdWhenIsStarred(
                            accountId,
                            content,
                            groupId,
                            true,
                        )

                    isUnread ->
                        articleDao.searchArticleByGroupIdWhenIsUnread(
                            accountId,
                            content,
                            groupId,
                            true,
                            sortAscending,
                        )

                    else -> articleDao.searchArticleByGroupIdWhenAll(accountId, content, groupId)
                }

            feedId != null ->
                when {
                    isStarred ->
                        articleDao.searchArticleByFeedIdWhenIsStarred(
                            accountId,
                            content,
                            feedId,
                            true,
                        )

                    isUnread ->
                        articleDao.searchArticleByFeedIdWhenIsUnread(
                            accountId,
                            content,
                            feedId,
                            true,
                            sortAscending,
                        )

                    else -> articleDao.searchArticleByFeedIdWhenAll(accountId, content, feedId)
                }

            else ->
                when {
                    isStarred -> articleDao.searchArticleWhenIsStarred(accountId, content, true)
                    isUnread ->
                        articleDao.searchArticleWhenIsUnread(
                            accountId,
                            content,
                            true,
                            sortAscending,
                        )

                    else -> articleDao.searchArticleWhenAll(accountId, content)
                }
        }
    }

    suspend fun queryUnreadFullContentArticles() =
        articleDao.queryUnreadFullContentArticles(accountService.getCurrentAccountId())
}

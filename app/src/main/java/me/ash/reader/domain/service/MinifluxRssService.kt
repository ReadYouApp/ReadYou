package me.ash.reader.domain.service

import android.content.Context
import androidx.compose.ui.util.fastFilter
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.rometools.rome.feed.synd.SyndFeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.security.MinifluxSecurityKey
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.miniflux.MinifluxAPI
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.spacerDollar
import me.ash.reader.ui.ext.toRFC3339Date
import java.util.Date
import javax.inject.Inject

class MinifluxRssService
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val groupDao: GroupDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    workManager: WorkManager,
    private val accountService: AccountService,
) :
    AbstractRssRepository(
        articleDao,
        groupDao,
        feedDao,
        workManager,
        rssHelper,
        notificationHelper,
        ioDispatcher,
        defaultDispatcher,
        accountService,
    ) {

    override val importSubscription: Boolean = true
    override val addSubscription: Boolean = true
    override val moveSubscription: Boolean = true
    override val deleteSubscription: Boolean = true
    override val updateSubscription: Boolean = true

    private suspend fun getMinifluxAPI() =
        MinifluxSecurityKey(accountService.getCurrentAccount().securityKey).run {
            MinifluxAPI.getInstance(
                context = context,
                serverUrl = serverUrl!!,
                apiToken = apiToken,
                username = username,
                password = password,
                clientCertificateAlias = clientCertificateAlias,
            )
        }

    override suspend fun validCredentials(account: Account): Boolean =
        MinifluxSecurityKey(account.securityKey).run {
            MinifluxAPI.getInstance(
                context = context,
                serverUrl = serverUrl!!,
                apiToken = apiToken,
                username = username,
                password = password,
                clientCertificateAlias = clientCertificateAlias,
            ).validCredentials()
        }

    override suspend fun clearAuthorization() {
        MinifluxAPI.clearInstance()
    }

    override suspend fun subscribe(
        feedLink: String,
        searchedFeed: SyndFeed,
        groupId: String,
        isNotification: Boolean,
        isFullContent: Boolean,
        isBrowser: Boolean,
    ) {
        val minifluxAPI = getMinifluxAPI()
        val categoryId = groupId.dollarLast().toLongOrNull()
        val response = minifluxAPI.createFeed(feedLink, categoryId)
        val feedObj = minifluxAPI.getFeed(response.feedId)
        val accountId = accountService.getCurrentAccountId()

        val feed = Feed(
            id = accountId.spacerDollar(feedObj.id),
            name = feedObj.title.decodeHTML() ?: searchedFeed.title.decodeHTML() ?: "",
            icon = null,
            url = feedObj.feedUrl,
            groupId = feedObj.category?.id?.let { accountId.spacerDollar(it) } ?: groupId,
            accountId = accountId,
            isNotification = isNotification,
            isFullContent = isFullContent,
            isBrowser = isBrowser,
        )
        feedDao.insert(feed)
    }

    override suspend fun addGroup(destFeed: Feed?, newGroupName: String): String {
        val minifluxAPI = getMinifluxAPI()
        val category = minifluxAPI.createCategory(newGroupName)
        val accountId = accountService.getCurrentAccountId()
        val groupIdStr = accountId.spacerDollar(category.id)
        groupDao.insert(Group(id = groupIdStr, name = category.title, accountId = accountId))
        destFeed?.let { moveFeed(groupIdStr, it) }
        return groupIdStr
    }

    override suspend fun renameGroup(group: Group) {
        val minifluxAPI = getMinifluxAPI()
        val categoryId = group.id.dollarLast().toLong()
        minifluxAPI.updateCategory(categoryId, group.name)
        groupDao.update(group)
    }

    override suspend fun renameFeed(feed: Feed) {
        val minifluxAPI = getMinifluxAPI()
        val feedId = feed.id.dollarLast().toLong()
        minifluxAPI.updateFeed(feedId, title = feed.name)
        feedDao.update(feed)
    }

    override suspend fun deleteGroup(group: Group, onlyDeleteNoStarred: Boolean?) {
        val minifluxAPI = getMinifluxAPI()
        val categoryId = group.id.dollarLast().toLong()
        minifluxAPI.deleteCategory(categoryId)
        groupDao.delete(group)
    }

    override suspend fun deleteFeed(feed: Feed, onlyDeleteNoStarred: Boolean?) {
        val minifluxAPI = getMinifluxAPI()
        val feedId = feed.id.dollarLast().toLong()
        minifluxAPI.removeFeed(feedId)
        feedDao.delete(feed)
    }

    override suspend fun moveFeed(originGroupId: String, feed: Feed) {
        val minifluxAPI = getMinifluxAPI()
        val feedId = feed.id.dollarLast().toLong()
        val targetCategoryId = originGroupId.dollarLast().toLong()
        minifluxAPI.updateFeed(feedId, categoryId = targetCategoryId)
        feedDao.update(feed.copy(groupId = originGroupId))
    }

    override suspend fun changeFeedUrl(feed: Feed) {
        val minifluxAPI = getMinifluxAPI()
        val feedId = feed.id.dollarLast().toLong()
        minifluxAPI.updateFeed(feedId)
        feedDao.update(feed)
    }

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        val minifluxAPI = getMinifluxAPI()
        val statusStr = if (isUnread) "unread" else "read"

        if (articleId != null) {
            val rawId = articleId.dollarLast().toLongOrNull() ?: return
            minifluxAPI.updateEntries(listOf(rawId), status = statusStr)
        } else if (feedId != null) {
            val rawFeedId = feedId.dollarLast().toLongOrNull() ?: return
            val unreadIds = minifluxAPI.getEntries(status = if (isUnread) "read" else "unread", feedId = rawFeedId).entries.map { it.id }
            minifluxAPI.updateEntries(unreadIds, status = statusStr)
        } else if (groupId != null) {
            val rawCategoryId = groupId.dollarLast().toLongOrNull() ?: return
            if (!isUnread) {
                minifluxAPI.markCategoryAsRead(rawCategoryId)
            }
        }
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        val minifluxAPI = getMinifluxAPI()
        val rawId = articleId.dollarLast().toLongOrNull() ?: return
        minifluxAPI.updateEntries(listOf(rawId), starred = isStarred)
    }

    override suspend fun syncReadStatus(articleIds: Set<String>, isUnread: Boolean): Set<String> {
        val minifluxAPI = getMinifluxAPI()
        val syncedEntries = mutableSetOf<String>()
        val statusStr = if (isUnread) "unread" else "read"
        articleIds
            .takeIf { it.isNotEmpty() }
            ?.chunked(100)
            ?.forEach { idList ->
                try {
                    val rawIds = idList.mapNotNull { it.dollarLast().toLongOrNull() }
                    if (rawIds.isNotEmpty()) {
                        minifluxAPI.updateEntries(rawIds, status = statusStr)
                        syncedEntries += idList
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        return syncedEntries
    }

    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?,
    ): ListenableWorker.Result = withContext(ioDispatcher) {
        try {
            val preTime = System.currentTimeMillis()
            val preDate = Date(preTime)
            val account = accountService.getAccountById(accountId)!!
            check(account.type.id == AccountType.Miniflux.id) { "Account type is invalid" }

            val minifluxAPI = getMinifluxAPI()

            // 1. Fetch categories
            val categories = minifluxAPI.getCategories()
            val groups = categories.map {
                Group(
                    id = accountId.spacerDollar(it.id),
                    name = it.title,
                    accountId = accountId,
                )
            }
            groupDao.insertOrUpdate(groups)

            // 2. Fetch feeds & favicons (concurrently)
            val remoteFeeds = minifluxAPI.getFeeds()
            val faviconsById = remoteFeeds
                .mapNotNull { remoteFeed ->
                    if (remoteFeed.icon != null) {
                        async(ioDispatcher) {
                            try {
                                val iconObj = minifluxAPI.getFeedIcon(remoteFeed.id)
                                remoteFeed.id to iconObj.data
                            } catch (_: Exception) {
                                null
                            }
                        }
                    } else null
                }
                .awaitAll()
                .filterNotNull()
                .toMap()

            val feeds = remoteFeeds.map {
                Feed(
                    id = accountId.spacerDollar(it.id),
                    name = it.title.decodeHTML() ?: context.getString(R.string.empty),
                    icon = faviconsById[it.id],
                    url = it.feedUrl,
                    groupId = accountId.spacerDollar(it.category?.id ?: 0),
                    accountId = accountId,
                )
            }
            feedDao.insertOrUpdate(feeds)

            // 3. Fetch entries
            var offset = 0
            val limit = 100
            val allArticles = mutableListOf<Article>()

            while (true) {
                val response = minifluxAPI.getEntries(limit = limit, offset = offset)
                if (response.entries.isEmpty()) break

                val articles = response.entries.map { entry ->
                    val pubDate = entry.publishedAt.toRFC3339Date() ?: preDate
                    Article(
                        id = accountId.spacerDollar(entry.id),
                        date = pubDate,
                        title = entry.title.decodeHTML() ?: context.getString(R.string.empty),
                        author = entry.author,
                        rawDescription = entry.content ?: "",
                        shortDescription = Readability.parseToText(entry.content, entry.url).take(280),
                        img = rssHelper.findThumbnail(entry.content),
                        link = entry.url,
                        feedId = accountId.spacerDollar(entry.feedId),
                        accountId = accountId,
                        isUnread = entry.status == "unread",
                        isStarred = entry.starred,
                        updateAt = preDate,
                    )
                }
                allArticles.addAll(articles)
                offset += response.entries.size
                if (response.entries.size < limit || offset >= 500) break
            }

            if (allArticles.isNotEmpty()) {
                articleDao.insert(*allArticles.toTypedArray())
                val notificationFeeds =
                    feedDao.queryNotificationEnabled(accountId).associateBy { it.id }
                val notificationFeedIds = notificationFeeds.keys
                allArticles
                    .fastFilter { it.isUnread && it.feedId in notificationFeedIds }
                    .groupBy { it.feedId }
                    .mapKeys { (feedId, _) -> notificationFeeds[feedId]!! }
                    .forEach { (feed, articles) -> notificationHelper.notify(feed, articles) }
            }

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            ListenableWorker.Result.failure()
        }
    }
}

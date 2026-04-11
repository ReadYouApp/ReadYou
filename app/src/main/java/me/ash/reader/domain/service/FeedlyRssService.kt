package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastFilter
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.rometools.rome.feed.synd.SyndFeed
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import me.ash.reader.R
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.security.FeedlySecurityKey
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.di.MainDispatcher
import me.ash.reader.infrastructure.exception.FeedlyAPIException
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.feedly.FeedlyAPI
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.isFuture
import me.ash.reader.ui.ext.spacerDollar

private const val TAG = "FeedlyRssService"

class FeedlyRssService
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val groupDao: GroupDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
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

    override val importSubscription: Boolean = false
    override val addSubscription: Boolean = true
    override val moveSubscription: Boolean = true
    override val deleteSubscription: Boolean = true
    override val updateSubscription: Boolean = true

    private suspend fun getFeedlyAPI(): FeedlyAPI =
        FeedlySecurityKey(accountService.getCurrentAccount().securityKey).run {
            FeedlyAPI.getInstance(
                context = context,
                accessToken = accessToken ?: throw FeedlyAPIException("Access token is not set"),
            )
        }

    override suspend fun validCredentials(account: Account): Boolean =
        try {
            val key = FeedlySecurityKey(account.securityKey)
            if (key.accessToken.isNullOrBlank()) return false
            val api = FeedlyAPI.getInstance(context, key.accessToken!!)
            val profile = api.getProfile()
            val userId = profile.id
            if (!userId.isNullOrBlank() && userId != key.userId) {
                accountService.update(account.copy(securityKey = FeedlySecurityKey(key.accessToken, userId).toString()))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "validCredentials failed: ${e.message}", e)
            false
        }

    override suspend fun clearAuthorization() {
        FeedlyAPI.clearInstance()
    }

    override suspend fun subscribe(
        feedLink: String,
        searchedFeed: SyndFeed,
        groupId: String,
        isNotification: Boolean,
        isFullContent: Boolean,
        isBrowser: Boolean,
    ) {
        val accountId = accountService.getCurrentAccountId()
        val key = FeedlySecurityKey(accountService.getCurrentAccount().securityKey)
        val userId = key.userId ?: throw FeedlyAPIException("User ID is not available")
        val api = getFeedlyAPI()

        val feedId = "feed/$feedLink"
        val categoryId = "user/$userId/category/${groupId.dollarLast()}"

        val subscription =
            api.addSubscription(
                feedId = feedId,
                title = searchedFeed.title,
                categoryId = categoryId,
            )

        feedDao.insert(
            Feed(
                id = accountId.spacerDollar(feedId),
                name = subscription.title ?: searchedFeed.title ?: context.getString(R.string.empty),
                url = feedLink,
                groupId = groupId,
                accountId = accountId,
                icon = subscription.iconUrl,
                isNotification = isNotification,
                isFullContent = isFullContent,
                isBrowser = isBrowser,
            )
        )
    }

    override suspend fun addGroup(destFeed: Feed?, newGroupName: String): String {
        val accountId = accountService.getCurrentAccountId()
        val key = FeedlySecurityKey(accountService.getCurrentAccount().securityKey)
        val userId = key.userId ?: throw FeedlyAPIException("User ID is not available")
        val categoryId = "user/$userId/category/$newGroupName"
        val id = accountId.spacerDollar(categoryId)
        groupDao.insert(Group(id = id, name = newGroupName, accountId = accountId))
        return id
    }

    override suspend fun renameFeed(feed: Feed) {
        super.renameFeed(feed)
    }

    override suspend fun moveFeed(originGroupId: String, feed: Feed) {
        val api = getFeedlyAPI()
        val key = FeedlySecurityKey(accountService.getCurrentAccount().securityKey)
        val userId = key.userId ?: throw FeedlyAPIException("User ID is not available")
        val feedId = feed.id.dollarLast()
        val newCategoryId = "user/$userId/category/${feed.groupId.dollarLast()}"
        api.addSubscription(feedId = feedId, title = feed.name, categoryId = newCategoryId)
        super.moveFeed(originGroupId, feed)
    }

    override suspend fun changeFeedUrl(feed: Feed) {
        throw FeedlyAPIException("Unsupported")
    }

    override suspend fun deleteGroup(group: Group, onlyDeleteNoStarred: Boolean?) {
        feedDao.queryByGroupId(accountService.getCurrentAccountId(), group.id).forEach {
            deleteFeed(it, onlyDeleteNoStarred)
        }
        super.deleteGroup(group, false)
    }

    override suspend fun deleteFeed(feed: Feed, onlyDeleteNoStarred: Boolean?) {
        getFeedlyAPI().deleteSubscription(feed.id.dollarLast())
        super.deleteFeed(feed, false)
    }

    /**
     * Feedly sync strategy:
     * 1. Fetch collections (categories/folders) and upsert groups
     * 2. Fetch subscriptions and upsert feeds
     * 3. Fetch stream contents for global.all since last sync, paginating with continuation tokens
     * 4. Insert new articles; read/starred status comes directly from each stream item
     * 5. Remove orphaned groups and feeds
     */
    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?,
    ): ListenableWorker.Result = coroutineScope {
        try {
            val preTime = System.currentTimeMillis()
            val preDate = Date(preTime)
            val account = accountService.getAccountById(accountId)!!
            check(account.type.id == AccountType.Feedly.id) { "account type is invalid" }

            val key = FeedlySecurityKey(account.securityKey)
            val accessToken =
                key.accessToken ?: throw FeedlyAPIException("Access token is not set")
            val userId = key.userId ?: run {
                val profile = FeedlyAPI.getInstance(context, accessToken).getProfile()
                val id = profile.id ?: throw FeedlyAPIException("Unable to retrieve user ID")
                accountService.update(
                    account.copy(
                        securityKey = FeedlySecurityKey(accessToken, id).toString()
                    )
                )
                id
            }
            val feedlyAPI = FeedlyAPI.getInstance(context, accessToken)

            // 1. Fetch collections (categories/folders)
            val collections = feedlyAPI.getCollections()
            val remoteGroupIds = mutableSetOf<String>()

            val groups =
                collections.mapNotNull { collection ->
                    val id = collection.id ?: return@mapNotNull null
                    remoteGroupIds.add(accountId.spacerDollar(id))
                    Group(
                        id = accountId.spacerDollar(id),
                        name = collection.label ?: context.getString(R.string.empty),
                        accountId = accountId,
                    )
                }
            groupDao.insertOrUpdate(groups)

            // 2. Fetch subscriptions (feeds) and assign to groups
            val subscriptions = feedlyAPI.getSubscriptions()
            val remoteFeedIds = mutableSetOf<String>()

            val feeds =
                subscriptions.mapNotNull { subscription ->
                    val feedId = subscription.id ?: return@mapNotNull null
                    val firstCategoryId = subscription.categories?.firstOrNull()?.id
                    val groupDbId =
                        if (firstCategoryId != null) {
                            accountId.spacerDollar(firstCategoryId)
                        } else {
                            // If no category, skip — feed must be in a group
                            return@mapNotNull null
                        }
                    remoteFeedIds.add(accountId.spacerDollar(feedId))
                    Feed(
                        id = accountId.spacerDollar(feedId),
                        name = subscription.title?.decodeHTML()
                            ?: context.getString(R.string.empty),
                        url = feedId.removePrefix("feed/"),
                        groupId = groupDbId,
                        accountId = accountId,
                        icon = subscription.iconUrl,
                    )
                }
            feedDao.insertOrUpdate(feeds)

            // Handle feeds with no icon
            val noIconFeeds = feedDao.queryNoIcon(accountId)
            feedDao.update(
                *noIconFeeds
                    .map { it.copy(icon = rssHelper.queryRssIconLink(it.url)) }
                    .toTypedArray()
            )

            // 3. Fetch stream contents for all articles since last sync
            val allStreamId = "user/$userId/category/global.all"
            val newerThan = account.updateAt?.time

            val allArticles = mutableListOf<Article>()
            var continuation: String? = null
            val maxBatches = 40

            repeat(maxBatches) { _ ->
                val streamContents =
                    feedlyAPI.getStreamContents(
                        streamId = allStreamId,
                        count = 250,
                        continuation = continuation,
                        newerThan = newerThan,
                    )

                val items = streamContents.items
                if (items.isNullOrEmpty()) {
                    return@repeat
                }

                items.forEach { item ->
                    val entryId = item.id ?: return@forEach
                    val feedIdRaw = item.origin?.feedId ?: return@forEach
                    val link =
                        item.canonical?.firstOrNull()?.href
                            ?: item.alternate?.firstOrNull()?.href
                            ?: ""
                    val content = item.content?.content ?: item.summary?.content ?: ""
                    val isStarred =
                        item.tags?.any { it.id?.contains("global.saved") == true } == true

                    allArticles.add(
                        Article(
                            id = accountId.spacerDollar(entryId),
                            date =
                                item.published
                                    ?.let { Date(it) }
                                    ?.takeIf { !it.isFuture(preDate) } ?: preDate,
                            title =
                                item.title?.decodeHTML()
                                    ?: context.getString(R.string.empty),
                            author = item.author,
                            rawDescription = content,
                            shortDescription =
                                Readability.parseToText(content, link).take(280),
                            img =
                                item.visual?.url
                                    ?: item.thumbnail?.firstOrNull()?.url
                                    ?: rssHelper.findThumbnail(content),
                            link = link,
                            feedId = accountId.spacerDollar(feedIdRaw),
                            accountId = accountId,
                            isUnread = item.unread != false,
                            isStarred = isStarred,
                            updateAt = preDate,
                        )
                    )
                }

                continuation = streamContents.continuation
                if (continuation == null) return@repeat
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

            // 5. Remove orphaned groups and feeds
            groupDao.queryAll(accountId).forEach {
                if (!remoteGroupIds.contains(it.id)) {
                    super.deleteGroup(it, true)
                }
            }
            feedDao.queryAll(accountId).forEach {
                if (!remoteFeedIds.contains(it.id)) {
                    super.deleteFeed(it, true)
                }
            }

            Log.i(TAG, "sync completed in ${System.currentTimeMillis() - preTime}ms")
            accountService.update(account.copy(updateAt = preDate))
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "sync failed: ${e.message}", e)
            ListenableWorker.Result.failure()
        }
    }

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        val asOf = before?.time ?: System.currentTimeMillis()
        val api = getFeedlyAPI()
        when {
            groupId != null -> {
                if (isUnread) {
                    // Feedly does not support "keepUnread" at category level via markers,
                    // so we only handle the local update already done by super.
                } else {
                    api.markCategoryAsRead(groupId.dollarLast(), asOf)
                }
            }

            feedId != null -> {
                if (isUnread) {
                    // Feedly does not support keepUnread at feed level via markers.
                } else {
                    api.markFeedAsRead(feedId.dollarLast(), asOf)
                }
            }

            articleId != null -> {
                if (isUnread) {
                    api.markEntriesAsUnread(listOf(articleId.dollarLast()))
                } else {
                    api.markEntriesAsRead(listOf(articleId.dollarLast()))
                }
            }

            else -> {
                if (!isUnread) {
                    val key = FeedlySecurityKey(accountService.getCurrentAccount().securityKey)
                    val userId = key.userId ?: return
                    api.markCategoryAsRead("user/$userId/category/global.all", asOf)
                }
            }
        }
    }

    override suspend fun syncReadStatus(articleIds: Set<String>, isUnread: Boolean): Set<String> {
        val api = getFeedlyAPI()
        val remoteIds = articleIds.map { it.dollarLast() }
        return try {
            if (isUnread) {
                api.markEntriesAsUnread(remoteIds)
            } else {
                api.markEntriesAsRead(remoteIds)
            }
            articleIds
        } catch (e: Exception) {
            Log.e(TAG, "syncReadStatus failed: ${e.message}", e)
            emptySet()
        }
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        val api = getFeedlyAPI()
        val remoteId = articleId.dollarLast()
        if (isStarred) {
            api.markEntriesAsSaved(listOf(remoteId))
        } else {
            api.markEntriesAsUnsaved(listOf(remoteId))
        }
    }
}

package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFilteredMap
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.rometools.rome.feed.synd.SyndFeed
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.collections.chunked
import kotlin.collections.toSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.ash.reader.R
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.AccountType.Companion.FreshRSS
import me.ash.reader.domain.model.account.security.GoogleReaderSecurityKey
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
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.infrastructure.net.onFailure
import me.ash.reader.infrastructure.net.onSuccess
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.dbId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.isValidItemId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofCategoryIdToStreamId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofCategoryStreamIdToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.ofFeedStreamIdToId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.remoteId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderAPI.Companion.shortId
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderDTO
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.isFuture
import me.ash.reader.ui.ext.spacerDollar
import timber.log.Timber

private const val TAG = "GoogleReaderRssService"

class GoogleReaderRssService
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
    private val workManager: WorkManager,
    private val accountService: AccountService,
    private val syncLogger: SyncLogger,
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

    private suspend fun getGoogleReaderAPI() =
        GoogleReaderSecurityKey(accountService.getCurrentAccount().securityKey).run {
            GoogleReaderAPI.getInstance(
                context = context,
                serverUrl = serverUrl!!,
                username = username!!,
                password = password!!,
                httpUsername = null,
                httpPassword = null,
                clientCertificateAlias = clientCertificateAlias,
                syncLogger = syncLogger,
            )
        }

    override suspend fun validCredentials(account: Account): Boolean {
        return getGoogleReaderAPI().validCredentials().also { success ->
            if (success)
                try {
                    getGoogleReaderAPI().getUserInfo().userName?.let {
                        accountService.update(account.copy(name = it))
                    }
                } catch (ignore: Exception) {
                    Log.e("RLog", "get user info is failed: ", ignore)
                }
        }
    }

    override suspend fun clearAuthorization() {
        GoogleReaderAPI.clearInstance()
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
        val quickAdd = getGoogleReaderAPI().subscriptionQuickAdd(feedLink)
        val feedId = quickAdd.streamId?.ofFeedStreamIdToId()
        requireNotNull(feedId) { "feedId is null" }
        val feedTitle = searchedFeed.title
        requireNotNull(feedTitle) { "feedTitle is null" }

        getGoogleReaderAPI()
            .subscriptionEdit(
                destFeedId = feedId,
                destCategoryId = groupId.dollarLast(),
                destFeedName = feedTitle,
            )
        feedDao.insert(
            Feed(
                id = accountId.spacerDollar(feedId),
                name = feedTitle,
                url = feedLink,
                groupId = groupId,
                accountId = accountId,
                isNotification = isNotification,
                isFullContent = isFullContent,
                isBrowser = isBrowser,
            )
        )
        // TODO: When users need to subscribe to multiple feeds continuously, this makes them
        // uncomfortable.
        //  It is necessary to make syncWork support synchronizing individual specified feeds.
        // super.doSyncOneTime()
    }

    override suspend fun addGroup(destFeed: Feed?, newGroupName: String): String {
        val accountId = accountService.getCurrentAccountId()
        getGoogleReaderAPI()
            .subscriptionEdit(
                destFeedId = destFeed?.id?.dollarLast(),
                destCategoryId = newGroupName,
            )
        val id = accountId.spacerDollar(newGroupName.ofCategoryIdToStreamId())
        groupDao.insert(Group(id = id, name = newGroupName, accountId = accountId))
        return id
    }

    override suspend fun renameGroup(group: Group) {
        getGoogleReaderAPI()
            .renameTag(categoryId = group.id.dollarLast(), renameToName = group.name)
        // TODO: Whether to switch the old ID to the new ID?
        super.renameGroup(group)
    }

    override suspend fun moveFeed(originGroupId: String, feed: Feed) {
        getGoogleReaderAPI()
            .subscriptionEdit(
                destFeedId = feed.id.dollarLast(),
                destCategoryId = feed.groupId.dollarLast().ofCategoryStreamIdToId(),
                originCategoryId = originGroupId.dollarLast().ofCategoryStreamIdToId(),
            )
        super.moveFeed(originGroupId, feed)
    }

    override suspend fun changeFeedUrl(feed: Feed) {
        throw Exception("Unsupported")
    }

    override suspend fun renameFeed(feed: Feed) {
        getGoogleReaderAPI()
            .subscriptionEdit(destFeedId = feed.id.dollarLast(), destFeedName = feed.name)
        // TODO: Whether to switch the old ID to the new ID?
        super.renameFeed(feed)
    }

    override suspend fun deleteGroup(group: Group, onlyDeleteNoStarred: Boolean?) {
        feedDao.queryByGroupId(accountService.getCurrentAccountId(), group.id).forEach {
            deleteFeed(it)
        }
        getGoogleReaderAPI().disableTag(group.id.dollarLast())
        super.deleteGroup(group, false)
    }

    override suspend fun deleteFeed(feed: Feed, onlyDeleteNoStarred: Boolean?) {
        getGoogleReaderAPI()
            .subscriptionEdit(action = "unsubscribe", destFeedId = feed.id.dollarLast())
        super.deleteFeed(feed, false)
    }

    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?,
    ): ListenableWorker.Result {
        return if (feedId != null) {
            syncFeed(accountId, feedId)
        } else {
            sync(accountId)
        }
    }

    /**
     * This is improved from Reeder's synchronization strategy, which syncs well across multiple
     * devices.
     * 1. Fetch tags (not supported yet)
     * 2. Fetch folder and subscription list
     * 3. Fetch all unread item id list
     * 4. Fetch all starred item id list
     * 5. Fetch unread contents of items with differences (up to 10k items per sync process)
     * 6. Fetch starred contents of items with differences
     * 7. Fetch read contents of items with differences (up to one month old)
     * 8. Remove orphaned groups and feeds, after synchronizing the starred/un-starred
     *
     * The following link contains other great synchronization logic, but it was not adopted due to
     * the solidified domain model of this application.
     *
     * @link https://github.com/FreshRSS/FreshRSS/issues/2566#issuecomment-541317776
     * @link https://github.com/bazqux/bazqux-api?tab=readme-ov-file
     * @link https://github.com/theoldreader/api
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun sync(accountId: Int): ListenableWorker.Result = coroutineScope {
        val preTime = System.currentTimeMillis()
        val preDate = Date(preTime)

        try {
            val account = accountService.getAccountById(accountId)
            requireNotNull(account) { "cannot find account" }
            check(
                account.type.id == AccountType.GoogleReader.id ||
                    account.type.id == AccountType.FreshRSS.id
            ) {
                "account type is invalid"
            }
            val googleReaderAPI = getGoogleReaderAPI()
            googleReaderAPI.refreshCredentialsIfNeeded()
            val lastMonthAt =
                Calendar.getInstance()
                    .apply {
                        time = preDate
                        add(Calendar.MONTH, -1)
                    }
                    .time
                    .time / 1000

            val remoteUnreadIds = async {
                fetchItemIdsAndContinue { googleReaderAPI.getUnreadItemIds(continuationId = it) }
                    .map { it.shortId }
                    .toSet()
            }

            val remoteStarredIds = async {
                fetchItemIdsAndContinue { googleReaderAPI.getStarredItemIds(continuationId = it) }
                    .map { it.shortId }
                    .toSet()
            }

            val isFreshRss = account.type.id == FreshRSS.id
            val remoteReadIds = async {
                fetchItemIdsAndContinue {
                        googleReaderAPI.getReadItemIds(
                            since = lastMonthAt,
                            continuationId = it,
                            useIt = isFreshRss,
                        )
                    }
                    .map { it.shortId }
                    .toSet()
            }

            val localAllItems = articleDao.queryMetadataAll(accountId)
            val localUnreadIds =
                localAllItems.filter { it.isUnread }.map { it.id.dollarLast() }.toSet()
            val localStarredIds =
                localAllItems.filter { it.isStarred }.map { it.id.dollarLast() }.toSet()

            val localReadIds =
                localAllItems.filter { !it.isUnread }.map { it.id.dollarLast() }.toSet()

            val localItemIds = localAllItems.map { it.id.dollarLast() }.toSet()

            //            launch {
            //                val toBeStarredRemote = localStarredIds - remoteStarredIds.await()
            //                if (toBeStarredRemote.isNotEmpty()) {
            //                    googleReaderAPI.editTag(
            //                        itemIds = toBeStarredRemote.toList(),
            //                        mark = GoogleReaderAPI.Stream.Starred.tag,
            //                    )
            //                }
            //            }

            launch {
                val toBeStarredLocal =
                    (localItemIds - localStarredIds)
                        .intersect(remoteStarredIds.await())
                        .map { accountId spacerDollar it }
                        .toSet()
                articleDao.markAsStarredByIdSet(
                    accountId = accountId,
                    ids = toBeStarredLocal,
                    isStarred = true,
                )
            }

            launch {
                val toBeUnstarredLocal =
                    (localStarredIds - remoteStarredIds.await())
                        .map { accountId spacerDollar it }
                        .toSet()
                articleDao.markAsStarredByIdSet(
                    accountId = accountId,
                    ids = toBeUnstarredLocal,
                    isStarred = false,
                )
            }

            launch {
                val toBeReadLocal =
                    remoteReadIds.await().intersect(localUnreadIds).map {
                        accountId spacerDollar it
                    }
                toBeReadLocal.chunked(1000).forEach {
                    articleDao.markAsReadByIdSet(
                        accountId = accountId,
                        ids = it.toSet(),
                        isUnread = false,
                    )
                }
            }

            launch {
                val toBeUnreadLocal =
                    localReadIds.intersect(remoteUnreadIds.await()).map {
                        accountId spacerDollar it
                    }
                toBeUnreadLocal.chunked(1000).forEach {
                    articleDao.markAsReadByIdSet(
                        accountId = accountId,
                        ids = it.toSet(),
                        isUnread = true,
                    )
                }
            }

            //
            //            launch {
            //                val toBeReadRemote = localReadIds.intersect(remoteUnreadIds.await())
            //                if (toBeReadRemote.isNotEmpty()) {
            //                    googleReaderAPI.editTag(
            //                        itemIds = toBeReadRemote.toList(),
            //                        mark = GoogleReaderAPI.Stream.Read.tag,
            //                    )
            //                }
            //            }

            // 2. Fetch folder and subscription list
            val groupWithFeedsMap = async {
                val subscriptionList = googleReaderAPI.getSubscriptionList()
                requireNotNull(subscriptionList) { "subscriptionList is null" }
                subscriptionList.subscriptions
                    .groupBy { it.categories?.firstOrNull() }
                    .mapKeys { (category, _) ->
                        val defaultGroup = accountService.getDefaultGroup()
                        val categoryId = category?.id
                        if (categoryId != null) {
                            val groupId = accountId spacerDollar categoryId.ofCategoryStreamIdToId()
                            Group(
                                id = groupId,
                                name = category.label.toString(),
                                accountId = accountId,
                            )
                        } else defaultGroup
                    }
                    .mapValues { (group, feeds) ->
                        feeds.map {
                            requireNotNull(it.id) { "feed id is null" }
                            val feedUrl = it.url ?: it.htmlUrl
                            requireNotNull(feedUrl) { "feed url is null" }
                            val feedId = accountId spacerDollar it.id.ofFeedStreamIdToId()
                            Feed(
                                id = feedId,
                                name = it.title.decodeHTML() ?: context.getString(R.string.empty),
                                url = feedUrl,
                                groupId = group.id,
                                accountId = accountId,
                                icon = it.iconUrl,
                            )
                        }
                    }
                    .toSortedMap { c1, c2 -> c1?.name.toString().compareTo(c2?.name.toString()) }
            }

            val toBeSync = async {
                (listOf(remoteUnreadIds, remoteStarredIds, remoteReadIds).awaitAll().flatten() -
                        localItemIds)
                    .toSet()
            }

            val deferredList =
                fetchItemsContentsDeferred(
                        itemIds = toBeSync.await(),
                        googleReaderAPI = googleReaderAPI,
                        accountId = accountId,
                        unreadIds = remoteUnreadIds.await(),
                        starredIds = remoteStarredIds.await(),
                        scope = this,
                    )
                    .toMutableList()

            val remoteGroups = async { groupWithFeedsMap.await().keys.toList() }
            val remoteFeeds = async { groupWithFeedsMap.await().values.flatten() }

            // Handle empty icon for feeds
            launch {
                val localFeeds = feedDao.queryAll(accountId)
                val remoteFeeds = remoteFeeds.await()
                val newFeeds = remoteFeeds.filter { feed -> feed.id !in localFeeds.map { it.id } }

                val feedsWithIconFetched =
                    newFeeds
                        .filter { it.icon == null }
                        .map { feed ->
                            async { feed.copy(icon = rssHelper.queryRssIconLink(feed.url)) }
                        }
                feedsWithIconFetched
                    .awaitAll()
                    .filterNot { it.icon.isNullOrEmpty() }
                    .also { feedDao.update(*it.toTypedArray()) }
            }

            groupDao.insertOrUpdate(remoteGroups.await())
            feedDao.insertOrUpdate(remoteFeeds.await())

            val notificationFeeds =
                feedDao.queryNotificationEnabled(accountId).associateBy { it.id }
            val notificationFeedIds = notificationFeeds.keys
            val articlesToNotify = mutableListOf<Article>()

            if (deferredList.isNotEmpty()) {
                launch {
                        whileSelect {
                            for (deferred in deferredList) {
                                deferred.onAwait {
                                    articleDao.insertList(it)
                                    articlesToNotify.addAll(
                                        it.fastFilter {
                                            it.isUnread && notificationFeedIds.contains(it.feedId)
                                        }
                                    )
                                    deferredList.remove(deferred)
                                    deferredList.isNotEmpty()
                                }
                            }
                        }
                    }
                    .invokeOnCompletion {
                        launch {
                            articlesToNotify
                                .groupBy { it.feedId }
                                .mapKeys { (feedId, _) -> notificationFeeds[feedId]!! }
                                .forEach { (feed, articles) ->
                                    notificationHelper.notify(feed, articles)
                                }
                        }
                    }
            }

            // 8. Remove orphaned groups and feeds, after synchronizing the
            // starred/un-starred
            groupDao
                .queryAll(accountId)
                .filter { it.id !in remoteGroups.await().map { group -> group.id } }
                .forEach { super.deleteGroup(it, true) }
            feedDao
                .queryAll(accountId)
                .filter { it.id !in remoteFeeds.await().map { feed -> feed.id } }
                .forEach { super.deleteFeed(it, true) }

            accountService.update(account.copy(updateAt = Date()))
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Timber.tag("RLog").e(e, "On sync exception: ${e.message}")
            syncLogger.log(e)
            //                withContext(mainDispatcher) {
            //                    context.showToast(e.message) todo: find a good way to
            // notice user
            // the error
            //                }
            ListenableWorker.Result.retry()
        }
    }

    private suspend fun syncFeed(accountId: Int, feedId: String): ListenableWorker.Result =
        supervisorScope {
            val preTime = System.currentTimeMillis()
            val account = accountService.getAccountById(accountId)
            requireNotNull(account) { "cannot find account" }
            check(
                account.type.id == AccountType.GoogleReader.id ||
                    account.type.id == AccountType.FreshRSS.id
            ) {
                "account type is invalid"
            }
            val googleReaderAPI = getGoogleReaderAPI()

            val feed = feedDao.queryById(feedId)!!

            val localStarredIds =
                articleDao
                    .queryMetadataByFeedId(accountId, feedId, isUnread = true)
                    .map { it.id.remoteId }
                    .toSet()

            val localUnreadIds =
                articleDao
                    .queryMetadataByFeedId(accountId, feedId, isUnread = true)
                    .map { it.id.remoteId }
                    .toSet()

            val localReadIds =
                articleDao
                    .queryMetadataByFeedId(accountId, feedId, isUnread = false)
                    .map { it.id.remoteId }
                    .toSet()

            val localIds = (localReadIds + localUnreadIds).toSet()

            val remoteUnreadIds = async {
                fetchItemIdsAndContinue {
                        googleReaderAPI.getItemIdsForFeed(
                            feedId = feedId.dollarLast(),
                            filterRead = true,
                            continuationId = it,
                        )
                    }
                    .map { it.shortId }
                    .toSet()
            }

            val remoteAllIds = async {
                fetchItemIdsAndContinue {
                        googleReaderAPI.getItemIdsForFeed(
                            feedId = feedId.dollarLast(),
                            filterRead = false,
                            continuationId = it,
                        )
                    }
                    .map { it.shortId }
                    .toSet()
            }

            val remoteStarredIds = async {
                fetchItemIdsAndContinue { googleReaderAPI.getStarredItemIds(continuationId = it) }
                    .map { it.shortId }
                    .toSet()
            }

            val toFetch = remoteAllIds.await() - localIds

            val items =
                fetchItemsContents(
                    itemIds = toFetch,
                    googleReaderAPI = googleReaderAPI,
                    accountId = accountId,
                    unreadIds = remoteUnreadIds.await(),
                    starredIds = remoteStarredIds.await(),
                )

            if (feed.isNotification) {
                val articlesToNotify = items.fastFilter { it.isUnread }
                notificationHelper.notify(feed, articlesToNotify)
            }

            launch {
                val remoteReadIds = remoteAllIds.await() - remoteUnreadIds.await()
                val toBeReadIds = remoteReadIds.intersect(localUnreadIds)

                toBeReadIds
                    .map { it.dbId(accountId) }
                    .chunked(1000)
                    .forEach {
                        articleDao.markAsReadByIdSet(
                            accountId = accountId,
                            ids = it.toSet(),
                            isUnread = false,
                        )
                    }
            }

            launch {
                val toBeUnreadIds = localReadIds.intersect(remoteUnreadIds.await())
                toBeUnreadIds
                    .map { it.dbId(accountId) }
                    .chunked(1000)
                    .forEach {
                        articleDao.markAsReadByIdSet(
                            accountId = accountId,
                            ids = it.toSet(),
                            isUnread = true,
                        )
                    }
            }

            launch {
                val toBeStarred = remoteStarredIds.await().intersect(localIds) - localStarredIds

                toBeStarred
                    .map { it.dbId(accountId) }
                    .chunked(1000)
                    .forEach {
                        articleDao.markAsStarredByIdSet(
                            accountId = accountId,
                            ids = it.toSet(),
                            isStarred = true,
                        )
                    }
            }

            launch {
                val toBeUnstarred = localStarredIds - remoteStarredIds.await()
                toBeUnstarred
                    .map { it.dbId(accountId) }
                    .chunked(1000)
                    .forEach {
                        articleDao.markAsStarredByIdSet(
                            accountId = accountId,
                            ids = it.toSet(),
                            isStarred = false,
                        )
                    }
            }

            articleDao.insert(*items.toTypedArray())
            Timber.i("onCompletion: ${System.currentTimeMillis() - preTime}")

            ListenableWorker.Result.success()
        }

    private suspend fun fetchItemIdsAndContinue(
        getItemIdsFunc: suspend (continuationId: String?) -> GoogleReaderDTO.ItemIds?
    ): MutableList<String> {
        var result = getItemIdsFunc(null)
        val ids = result?.itemRefs?.mapNotNull { it.id }?.toMutableList() ?: return mutableListOf()
        while (result != null && result.continuation != null) {
            result = getItemIdsFunc(result.continuation)
            result?.itemRefs?.mapNotNull { it.id }?.let { ids.addAll(it) }
        }
        return ids
    }

    fun fetchItemsContentsDeferred(
        itemIds: Set<String>,
        googleReaderAPI: GoogleReaderAPI,
        accountId: Int,
        unreadIds: Set<String>,
        starredIds: Set<String>,
        scope: CoroutineScope,
    ): List<Deferred<List<Article>>> {
        if (itemIds.isEmpty()) return emptyList()
        val currentDate = Date()
        val semaphore = Semaphore(8)
        return itemIds.chunked(100).mapIndexed { index, chunkedIds ->
            scope.async(ioDispatcher) {
                semaphore.withPermit {
                    val result = googleReaderAPI.getItemsContents(chunkedIds)
                    val updated = result.updated
                    val fetchedItems = result.items
                    if (fetchedItems == null) return@async emptyList()
                    fetchedItems.fastFilteredMap(
                        predicate = { it.id?.isValidItemId() == true },
                        transform = {
                            val articleId = it.id!!.shortId
                            Article(
                                id = accountId.spacerDollar(articleId),
                                date =
                                    it.published
                                        ?.run { Date(this * 1000) }
                                        ?.takeIf { !it.isFuture(currentDate) } ?: currentDate,
                                title = it.title.decodeHTML() ?: context.getString(R.string.empty),
                                author = it.author,
                                rawDescription = it.summary?.content ?: "",
                                shortDescription =
                                    Readability.parseToText(it.summary?.content, findArticleURL(it))
                                        .take(280),
                                //                        fullContent = it.summary?.content
                                // ?:
                                // "",
                                img = rssHelper.findThumbnail(it.summary?.content),
                                link = findArticleURL(it),
                                feedId =
                                    accountId.spacerDollar(
                                        it.origin?.streamId?.ofFeedStreamIdToId()!!
                                    ),
                                accountId = accountId,
                                isUnread = unreadIds.contains(articleId),
                                isStarred = starredIds.contains(articleId),
                                updateAt =
                                    updated?.let { Date(updated * 1000L) }
                                        ?: it.crawlTimeMsec?.let { Date(it.toLong()) }
                                        ?: currentDate,
                            )
                        },
                    )
                }
            }
        }
    }

    private suspend fun fetchItemsContents(
        itemIds: Set<String>,
        googleReaderAPI: GoogleReaderAPI,
        accountId: Int,
        unreadIds: Set<String>,
        starredIds: Set<String>,
    ): List<Article> = supervisorScope {
        fetchItemsContentsDeferred(
                itemIds = itemIds,
                googleReaderAPI = googleReaderAPI,
                accountId = accountId,
                unreadIds = unreadIds,
                starredIds = starredIds,
                scope = this,
            )
            .awaitAll()
            .flatten()
    }

    private fun findArticleURL(it: GoogleReaderDTO.Item) =
        it.canonical?.firstOrNull()?.href
            ?: it.alternate?.firstOrNull()?.href
            ?: it.origin?.htmlUrl
            ?: ""

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        val accountId = accountService.getCurrentAccountId()
        val googleReaderAPI = getGoogleReaderAPI()
        val markList: List<String> =
            when {
                groupId != null -> {
                    if (before == null) {
                            articleDao.queryMetadataByGroupIdWhenIsUnread(
                                accountId,
                                groupId,
                                !isUnread,
                            )
                        } else {
                            articleDao.queryMetadataByGroupIdWhenIsUnread(
                                accountId,
                                groupId,
                                !isUnread,
                                before,
                            )
                        }
                        .map { it.id.dollarLast() }
                }

                feedId != null -> {
                    if (before == null) {
                            articleDao.queryMetadataByFeedId(accountId, feedId, !isUnread)
                        } else {
                            articleDao.queryMetadataByFeedId(accountId, feedId, !isUnread, before)
                        }
                        .map { it.id.dollarLast() }
                }

                articleId != null -> {
                    listOf(articleId.dollarLast())
                }

                else -> {
                    if (before == null) {
                            articleDao.queryMetadataAll(accountId, !isUnread)
                        } else {
                            articleDao.queryMetadataAll(accountId, !isUnread, before)
                        }
                        .map { it.id.dollarLast() }
                }
            }
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        markList
            .takeIf { it.isNotEmpty() }
            ?.chunked(500)
            ?.forEachIndexed { index, it ->
                Log.d("RLog", "sync markAsRead:  ${(index * 500) + it.size}/${markList.size} num")
                googleReaderAPI.editTag(
                    itemIds = it,
                    mark = if (!isUnread) GoogleReaderAPI.Stream.Read.tag else null,
                    unmark = if (isUnread) GoogleReaderAPI.Stream.Read.tag else null,
                )
            }
    }

    override suspend fun syncReadStatus(articleIds: Set<String>, isUnread: Boolean): Set<String> {
        val googleReaderAPI = getGoogleReaderAPI()
        val syncedEntries = mutableSetOf<String>()
        articleIds
            .takeIf { it.isNotEmpty() }
            ?.chunked(500)
            ?.forEachIndexed { index, idList ->
                Log.d(
                    "RLog",
                    "sync markAsRead:  ${(index * 500) + idList.size}/${articleIds.size} num",
                )
                googleReaderAPI
                    .editTag(
                        itemIds = idList.map { it.dollarLast() },
                        mark = if (!isUnread) GoogleReaderAPI.Stream.Read.tag else null,
                        unmark = if (isUnread) GoogleReaderAPI.Stream.Read.tag else null,
                    )
                    .onFailure { it.printStackTrace() }
                    .onSuccess {
                        syncedEntries += idList
                        println("synced $idList to isUnread: $isUnread")
                    }
            }
        return syncedEntries
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        getGoogleReaderAPI()
            .editTag(
                itemIds = listOf(articleId.dollarLast()),
                mark = if (isStarred) GoogleReaderAPI.Stream.Starred.tag else null,
                unmark = if (!isStarred) GoogleReaderAPI.Stream.Starred.tag else null,
            )
    }
}

package me.ash.reader.domain.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import java.io.File
import javax.inject.Inject

private const val TAG = "DiffMapHolder"

@OptIn(FlowPreview::class)
class DiffMapHolder @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val accountService: AccountService,
    private val rssService: RssService,
) {
    val diffMap = mutableStateMapOf<String, Diff>()

    private val pendingSyncDiffs = mutableStateMapOf<String, Diff>()
    private val syncedDiffs = mutableMapOf<String, Diff>()

    val diffMapSnapshotFlow = snapshotFlow { diffMap.toMap() }.stateIn(
        applicationScope, SharingStarted.Eagerly, emptyMap()
    )

    private val pendingSyncDiffsSnapshotFlow = snapshotFlow { pendingSyncDiffs.toMap() }.stateIn(
        applicationScope, SharingStarted.Eagerly, emptyMap()
    )

    val shouldSyncWithRemote get() = currentAccount?.type != AccountType.Local

    private val gson = Gson()

    private val cacheDir = context.cacheDir.resolve("diff")
    private var userCacheDir = cacheDir

    private var currentAccount: Account? = null

    private val cacheFile: File get() = userCacheDir.resolve("diff_map.json")

    var dbJob: Job? = null
    var remoteJob: Job? = null

    init {
        applicationScope.launch {
            accountService.currentAccountFlow.mapNotNull { it }.collect { account ->
                val previousAccount = currentAccount
                if (previousAccount != null && previousAccount != account) {
                    cleanup(previousAccount)
                }
                currentAccount = account
                init(account)
            }
        }
    }

    private fun init(account: Account) {
        userCacheDir = cacheDir.resolve(account.id.toString())
        commitDiffsFromCache()
        commitOnChange()
        if (account.type != AccountType.Local) {
            syncOnChange()
        }
    }

    private fun cleanup(account: Account) {
        dbJob?.cancel()
        remoteJob?.cancel()
        writeDiffsToCache()
        diffMap.clear()
        pendingSyncDiffs.clear()
        syncedDiffs.clear()
    }

    private fun commitOnChange() {
        dbJob = applicationScope.launch(ioDispatcher) {
            diffMapSnapshotFlow.debounce(2_000).collect {
                if (it.isNotEmpty()) {
                    writeDiffsToCache()
                }
            }
        }
    }

    private fun syncOnChange() {
        remoteJob = applicationScope.launch(ioDispatcher) {
            pendingSyncDiffsSnapshotFlow.debounce(2_000).collect {
                withContext(ioDispatcher) {
                    syncDiffsWithRemote(it)
                }
            }
        }
    }

    fun checkIfUnread(articleWithFeed: ArticleWithFeed): Boolean {
        return diffMap[articleWithFeed.article.id]?.isUnread ?: articleWithFeed.article.isUnread
    }

    /**
     * Updates the diff map with changes to an article's read/unread status.
     *
     * This function manages a map (`diffMap`) that tracks pending changes (diffs) to the
     * read/unread status of articles. These changes are not immediately applied to the
     * underlying data store but are held in `diffMap` until a later commit operation.
     *
     * The function supports three modes of updating:
     *
     * 1. **Toggle:** If `isUnread` is `null`, the function toggles the current read/unread
     *    status of the article.  If the article is currently unread, it will be marked as read,
     *    and vice-versa.
     * 2. **Mark as Unread:** If `isUnread` is `true`, the article will be marked as unread,
     *    regardless of its current status.
     * 3. **Mark as Read:** If `isUnread` is `false`, the article will be marked as read,
     *    regardless of its current status.
     *
     * The function determines if a change needs to be tracked based on the current status and desired status:
     *  - If the requested change matches the article's current status, the diff is removed from the map, if it exists. (No change is needed.)
     *  - Otherwise, the diff is added to or updated in the map.
     *
     * @param articleWithFeed The article and its associated feed data. This is used to identify the article
     *                        and access its current read/unread state.
     * @param isUnread An optional boolean indicating the desired read/unread status of the article.
     *                 - `null`: Toggles the current read/unread status.
     *                 - `true`: Marks the article as unread.
     *                 - `false`: Marks the article as read.
     *
     * @return A [Diff] object representing the changes made to the article.
     *
     * @see Diff
     */
    private fun updateDiffInternal(
        articleWithFeed: ArticleWithFeed, isUnread: Boolean? = null
    ): Diff? {
        val articleId = articleWithFeed.article.id

        val diff = diffMap[articleId]

        if (diff == null) {
            val isUnread = isUnread ?: !articleWithFeed.article.isUnread
            val diff = Diff(
                isUnread = isUnread, articleWithFeed = articleWithFeed
            )
            diffMap[articleId] = diff
            return diff
        } else {
            if (isUnread == null || diff.isUnread != isUnread) {
                val diff = diffMap.remove(articleId)
                return diff?.copy(isUnread = !diff.isUnread)
            }
        }
        return null
    }

    fun updateDiff(
        vararg articleWithFeed: ArticleWithFeed, isUnread: Boolean? = null
    ) {
        val appliedDiffs = articleWithFeed.mapNotNull {
            updateDiffInternal(it, isUnread)
        }
        if (shouldSyncWithRemote) {
            appliedDiffs.forEach {
                appendDiffToSync(it)
            }
        }
    }

    private fun appendDiffToSync(diff: Diff) {
        val syncedDiff = syncedDiffs[diff.articleId]
        if (syncedDiff == null || syncedDiff.isUnread != diff.isUnread) {
            pendingSyncDiffs[diff.articleId] = diff
        }
    }

    fun commitDiffsToDb() {
        applicationScope.launch(ioDispatcher) {
            val markAsReadArticles = diffMap.filter { !it.value.isUnread }.map { it.key }.toSet()
            val markAsUnreadArticles = diffMap.filter { it.value.isUnread }.map { it.key }.toSet()
            clearDiffs()
            rssService.get().batchMarkAsRead(articleIds = markAsReadArticles, isUnread = false)
            rssService.get().batchMarkAsRead(articleIds = markAsUnreadArticles, isUnread = true)
        }
    }

    private fun writeDiffsToCache() {
        applicationScope.launch(ioDispatcher) {
            try {
                val tmpJson = gson.toJson(diffMap)
                userCacheDir.mkdirs()
                cacheFile.createNewFile()
                if (cacheFile.exists() && cacheFile.canWrite()) {
                    cacheFile.writeText(tmpJson)
                }
            } catch (_: Exception) {

            }
        }
    }

    private suspend fun syncDiffsWithRemote(diffs: Map<String, Diff>) {
        if (!shouldSyncWithRemote) return
        if (diffs.isEmpty()) return
        val toBeSync = diffs
        val markAsReadArticles =
            toBeSync.filter { !it.value.isUnread }.map { it.key }.toSet()
        val markAsUnreadArticles =
            toBeSync.filter { it.value.isUnread }.map { it.key }.toSet()

        val rssService = rssService.get()

        val synced = supervisorScope {
            val read = async {
                rssService.syncReadStatus(
                    articleIds = markAsReadArticles,
                    isUnread = false
                )
            }
            val unread = async {
                rssService.syncReadStatus(
                    articleIds = markAsUnreadArticles,
                    isUnread = true
                )
            }
            runCatching { read.await() }.getOrElse { emptySet() } +
                    runCatching { unread.await() }.getOrElse { emptySet() }
        }

        pendingSyncDiffs -= synced
        syncedDiffs += diffs.filter { synced.contains(it.key) }
    }

    private fun commitDiffsFromCache() {
        applicationScope.launch(ioDispatcher) {
            if (cacheFile.exists() && cacheFile.canRead()) {
                val tmpJson = cacheFile.readText()
                val mapType = object : TypeToken<Map<String, Diff>>() {}.type
                val diffMapFromCache = gson.fromJson<Map<String, Diff>>(
                    tmpJson, mapType
                )
                diffMapFromCache?.let {
                    diffMap.clear()
                    diffMap.putAll(it)
                }
            }
        }.invokeOnCompletion {
            commitDiffsToDb()
        }
    }

    private fun clearDiffs() {
        applicationScope.launch(ioDispatcher) {
            if (cacheFile.exists() && cacheFile.canWrite()) {
                cacheFile.delete()
            }
            diffMap.clear()
        }
    }
}

data class Diff(
    val isUnread: Boolean, val articleId: String, val feedId: String
) {
    constructor(isUnread: Boolean, articleWithFeed: ArticleWithFeed) : this(
        isUnread = isUnread,
        articleId = articleWithFeed.article.id,
        feedId = articleWithFeed.feed.id,
    )
}

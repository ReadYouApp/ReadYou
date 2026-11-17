package me.ash.reader.domain.data

import android.content.Context
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.service.AbstractRssRepository
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking

class DiffMapHolderTest {

    @get:Rule val cacheDir = TemporaryFolder()

    private val repository = mock<AbstractRssRepository>()

    private val articleWithFeed = articleWithFeed(id = "article", isUnread = true)

    private fun articleWithFeed(id: String, isUnread: Boolean) =
        ArticleWithFeed(
            article =
                Article(
                    id = id,
                    date = Date(0),
                    title = "",
                    rawDescription = "",
                    shortDescription = "",
                    link = "",
                    feedId = "feed",
                    accountId = 1,
                    isUnread = isUnread,
                ),
            feed = Feed(id = "feed", name = "", url = "", groupId = "group", accountId = 1),
        )

    private fun diffMapHolder(): DiffMapHolder {
        val context = mock<Context> { on { cacheDir } doReturn cacheDir.root }
        val accountService =
            mock<AccountService> {
                on { currentAccountFlow } doReturn MutableStateFlow<Account?>(null)
            }
        val rssService = mock<RssService> { on { get() } doReturn repository }
        return DiffMapHolder(
            context = context,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
            ioDispatcher = Dispatchers.Unconfined,
            accountService = accountService,
            rssService = rssService,
        )
    }

    @Test
    fun markAsReadNowWritesToDatabaseAndHoldsNoDiff() {
        val holder = diffMapHolder()

        holder.markAsReadNow(articleWithFeed)

        assertTrue(holder.diffMap.isEmpty())
        verifyBlocking(repository) { batchMarkAsRead(setOf("article"), false) }
    }

    @Test
    fun updateDiffHoldsDiffAndDoesNotWriteToDatabase() {
        val holder = diffMapHolder()

        holder.updateDiff(articleWithFeed)

        assertEquals(false, holder.diffMap["article"]?.isUnread)
        verifyBlocking(repository, never()) { batchMarkAsRead(any(), any()) }
    }

    @Test
    fun commitReadDiffsNowWritesReadDiffsOnlyAndRemovesThemAfterListRefresh() {
        val holder = diffMapHolder()
        holder.updateDiff(articleWithFeed)
        holder.updateDiff(articleWithFeed(id = "read", isUnread = false))

        var diffsDuringListRefresh = emptySet<String>()
        holder.commitReadDiffsNow { diffsDuringListRefresh = holder.diffMap.keys.toSet() }

        // The read diff must stay until the list reloads.
        assertEquals(setOf("article", "read"), diffsDuringListRefresh)
        assertEquals(setOf("read"), holder.diffMap.keys)
        verifyBlocking(repository) { batchMarkAsRead(setOf("article"), false) }
    }
}

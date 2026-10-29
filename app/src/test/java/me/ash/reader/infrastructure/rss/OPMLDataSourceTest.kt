package me.ash.reader.infrastructure.rss

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

internal const val OPML_TEMPLATE: String = """
<opml version="1.0">
    <head>
        <title>Import OPML Unit Test👿</title>
    </head>
     <body>
        {{var}}
    </body>
</opml>
"""

@RunWith(MockitoJUnitRunner::class)
class OPMLDataSourceTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockIODispatcher: CoroutineDispatcher

    private lateinit var opmlDataSource: OPMLDataSource

    private val defaultGroup = Group(id = "1", name = "Default", accountId = 1)

    private lateinit var mockObject: OPMLDataSourceTest

    @Before
    fun setUp() {
        mockContext = mock<Context> { }
        mockIODispatcher = mock<CoroutineDispatcher> {}
        opmlDataSource = OPMLDataSource(mockContext, mockIODispatcher)
    }

    private fun fill(value: String): String = OPML_TEMPLATE.replace("{{var}}", value)

    private fun parse(opml: String): List<GroupWithFeed> = runBlocking {
        opmlDataSource.parseFileInputStream(
            inputStream = opml.byteInputStream(Charsets.UTF_8),
            defaultGroup = defaultGroup,
            targetAccountId = 1
        )
    }

    @Test
    fun testEmptyTitle() {
        val opml = fill("""
            <outline text="Blogs" title="Blogs">
                <outline type="rss" xmlUrl="https://ash7.io/index.xml" htmlUrl="https://ash7.io"/>
            </outline>
        """)
        val result = parse(opml)
        Assert.assertEquals(2, result.size)
        Assert.assertEquals("Default", result[0].group.name)
        Assert.assertEquals(0, result[0].feeds.size)
        Assert.assertEquals("Blogs", result[1].group.name)
        Assert.assertEquals(1, result[1].feeds.size)
        Assert.assertEquals("ash7.io", result[1].feeds[0].name)
        Assert.assertEquals("https://ash7.io/index.xml", result[1].feeds[0].url)
    }

    @Test
    fun testTopLevelFeedUsesDefaultGroup() {
        val opml = fill("""
            <outline type="rss" text="Loose Feed" xmlUrl="https://loose.example/feed.xml"/>
        """)
        val result = parse(opml)
        Assert.assertEquals(1, result.size)
        Assert.assertEquals(defaultGroup.id, result[0].feeds[0].groupId)
        Assert.assertEquals("Loose Feed", result[0].feeds[0].name)
    }

    @Test
    fun testReadYouAttributesAndFallbackName() {
        val opml = fill("""
            <outline text="Blogs" title="Blogs">
                <outline type="rss" xmlUrl="https://ash7.io/index.xml"
                    isNotification="true" isFullContent="true" isBrowser="true"/>
            </outline>
        """)
        val feed = parse(opml)[1].feeds[0]
        Assert.assertEquals("ash7.io", feed.name)
        Assert.assertTrue(feed.isNotification)
        Assert.assertTrue(feed.isFullContent)
        Assert.assertTrue(feed.isBrowser)
    }
    @Test
    fun testNestedFoldersKeepEveryFeedInOuterGroup() {
        val result = parse(fill("""
            <outline text="Manga news">
                <outline text="Direct" xmlUrl="https://direct.example/rss"/>
                <outline text="Nested">
                    <outline text="Deep" xmlUrl="https://deep.example/rss"
                        isNotification="true" isFullContent="true" isBrowser="true"/>
                    <outline text="Deeper">
                        <outline text="Deepest" xmlUrl="https://deepest.example/rss"/>
                    </outline>
                </outline>
                <outline text="Last" xmlUrl="https://last.example/rss"/>
            </outline>
        """))
        Assert.assertEquals(listOf("Default", "Manga news"), result.map { it.group.name })
        val group = result[1]
        Assert.assertEquals(listOf("Direct", "Deep", "Deepest", "Last"), group.feeds.map { it.name })
        Assert.assertTrue(group.feeds.all { it.groupId == group.group.id && it.accountId == 1 })
        val deep = group.feeds[1]
        Assert.assertTrue(deep.isNotification)
        Assert.assertTrue(deep.isFullContent)
        Assert.assertTrue(deep.isBrowser)
    }

    @Test
    fun testNestedDefaultGroupUsesDefaultGroup() {
        val result = parse(fill("""
            <outline text="Default" isDefault="true">
                <outline text="Nested"><outline text="Default feed" xmlUrl="https://default.example/rss"/></outline>
            </outline>
        """))
        Assert.assertEquals(listOf("Default feed"), result[0].feeds.map { it.name })
        Assert.assertEquals(defaultGroup.id, result[0].feeds.single().groupId)
        Assert.assertEquals(1, result.size)
    }

    @Test
    fun testEmptyNestedFoldersDoNotHideSiblingFeeds() {
        val result = parse(fill("""
            <outline text="Outer">
                <outline text="Empty"><outline text="Also empty"/></outline>
                <outline text="Feed" xmlUrl="https://feed.example/rss"/>
            </outline>
        """))
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(listOf("Feed"), result[1].feeds.map { it.name })
    }

}

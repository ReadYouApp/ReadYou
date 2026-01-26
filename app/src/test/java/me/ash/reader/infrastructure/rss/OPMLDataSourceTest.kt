package me.ash.reader.infrastructure.rss

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

    private lateinit var ioDispatcher: CoroutineDispatcher

    private lateinit var opmlDataSource: OPMLDataSource

    private val defaultGroup = Group(id = "1", name = "Default", accountId = 1)

    private lateinit var mockObject: OPMLDataSourceTest

    @Before
    fun setUp() {
        mockContext = mock<Context> { }
        ioDispatcher = Dispatchers.Unconfined
        opmlDataSource = OPMLDataSource(mockContext, ioDispatcher)
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
}

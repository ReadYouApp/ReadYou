package me.ash.reader.infrastructure.rss

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import java.io.StringReader

internal const val enclosureUrlString1: String = "https://example.com/enclosure.jpg"
internal const val enclosureUrlString2: String = "https://github.blog/wp-content/uploads/2024/03/github_copilot_header.png"
internal const val imageUrlString: String = "https://example.com/image.jpg"
internal const val enclosureHtmlCase1: String = """
        <enclosure url="$enclosureUrlString1" type="image/jpeg"/>
        <img src="$imageUrlString"/>
    """
internal const val enclosureHtmlCase2: String = """
        <img src="$imageUrlString"/>
        <enclosure url="$enclosureUrlString1" type="image/jpeg"/>
        <img src="$imageUrlString"/> 
    """
internal const val enclosureHtmlCase3: String = """
        <img src="$imageUrlString"/>
        <enclosure url="$enclosureUrlString2" type="image/png"/>
        <img src="$imageUrlString"/> 
    """
internal const val imageHtmlCase1: String = """
        <img src="$enclosureUrlString1"/>
        <img src="$imageUrlString"/> 
    """
internal const val imageHtmlCase2: String = """
        <img src="$imageUrlString"/> 
        <img src="$enclosureUrlString1"/> 
        <img src="$enclosureUrlString1"/> 
    """

@RunWith(MockitoJUnitRunner::class)
class RssHelperTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockIODispatcher: CoroutineDispatcher

    @Mock
    private lateinit var mockOkHttpClient: OkHttpClient

    private lateinit var rssHelper: RssHelper

    @Before
    fun setUp() {
        mockContext = mock<Context> { }
        mockIODispatcher = mock<CoroutineDispatcher> {}
        mockOkHttpClient = mock<OkHttpClient> {}
        rssHelper = RssHelper(mockContext, mockIODispatcher, mockOkHttpClient)
    }

    @Test
    fun testFindThumbnail() {
        Assert.assertNull(rssHelper.findThumbnail(""))
        Assert.assertNull(rssHelper.findThumbnail(" "))
        Assert.assertNull(rssHelper.findThumbnail(null))
        Assert.assertEquals(enclosureUrlString1, rssHelper.findThumbnail(enclosureHtmlCase1))
        Assert.assertEquals(enclosureUrlString1, rssHelper.findThumbnail(enclosureHtmlCase2))
        Assert.assertEquals(enclosureUrlString2, rssHelper.findThumbnail(enclosureHtmlCase3))
        Assert.assertEquals(enclosureUrlString1, rssHelper.findThumbnail(imageHtmlCase1))
        Assert.assertEquals(imageUrlString, rssHelper.findThumbnail(imageHtmlCase2))
    }

    @Test
    fun testEnclosureNoFilenameExtension() {
        val case = """
            <enclosure url="$imageUrlString" type="image/jpeg" length="0"/>
        """
        Assert.assertEquals(imageUrlString, rssHelper.findThumbnail(case))
    }

    @Test
    fun testMediaNamespaceThumbnailInRSS20() {
        val case = """
            <enclosure url="$imageUrlString" type="image/jpeg" length="0"/>
        """
        Assert.assertEquals(imageUrlString, rssHelper.findThumbnail(case))
    }



    private fun parseMediaEntry(mediaXml: String): SyndEntry {
        val xml = """
            <rss version="2.0"
                 xmlns:media="http://search.yahoo.com/mrss/">
                <channel>
                    <title>Test feed</title>
                    <link>https://example.com/</link>
                    <description>Image selection tests</description>
                    <item>
                        <title>Test article</title>
                        <link>https://example.com/article</link>
                        $mediaXml
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        return StringReader(xml).use { reader ->
            SyndFeedInput().build(reader).entries.single()
        }
    }

    @Test
    fun testPrefersMediaContentOverThumbnail() {
        val entry = parseMediaEntry(
            """
                <media:thumbnail url="https://example.com/small.jpg" />
                <media:content url="https://example.com/large.jpg"
                               medium="image" type="image/jpeg" />
            """.trimIndent()
        )

        Assert.assertEquals(
            "https://example.com/large.jpg",
            rssHelper.findThumbnail(entry)
        )
    }

    @Test
    fun testFallsBackToMediaThumbnail() {
        val entry = parseMediaEntry(
            """<media:thumbnail url="https://example.com/small.jpg" />"""
        )

        Assert.assertEquals(
            "https://example.com/small.jpg",
            rssHelper.findThumbnail(entry)
        )
    }

    @Test
    fun testUsesThumbnailInsteadOfVideoContent() {
        val entry = parseMediaEntry(
            """
                <media:thumbnail url="https://example.com/small.jpg" />
                <media:content url="https://example.com/video.mp4"
                               medium="video" type="video/mp4" />
            """.trimIndent()
        )

        Assert.assertEquals(
            "https://example.com/small.jpg",
            rssHelper.findThumbnail(entry)
        )
    }

    @Test
    fun testReturnsNullWithoutMediaImages() {
        val entry = parseMediaEntry("")

        Assert.assertNull(rssHelper.findThumbnail(entry))
    }
}

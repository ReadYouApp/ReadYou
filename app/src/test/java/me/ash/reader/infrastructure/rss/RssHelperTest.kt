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

    @Test
    fun testParseRss20Feed() {
        val sampleFeedXml = """
            <?xml version="1.0"?>
            <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
             <channel>
                <title>Phoronix</title>
                <link>https://www.phoronix.com/</link>
                <description>Linux Hardware Reviews, Performance Benchmarks</description>
                <language>en-us</language>
              <item>
               <title>Sample Linux Article</title>
               <link>https://www.phoronix.com/news/sample-linux-article</link>
               <guid>https://www.phoronix.com/news/sample-linux-article</guid>
               <description>Sample article description.</description>
               <pubDate>Sun, 23 Aug 2026 07:41:04 -0400</pubDate>
               <dc:creator>Michael Larabel</dc:creator>
              </item>
             </channel>
            </rss>
        """.trimIndent()

        val inputStream = java.io.ByteArrayInputStream(sampleFeedXml.toByteArray(Charsets.UTF_8))
        val syndFeed = com.rometools.rome.io.SyndFeedInput().build(com.rometools.rome.io.XmlReader(inputStream, "text/xml; charset=UTF-8"))
        Assert.assertEquals("Phoronix", syndFeed.title)
        Assert.assertEquals(1, syndFeed.entries.size)
        Assert.assertEquals("Sample Linux Article", syndFeed.entries[0].title)
        Assert.assertEquals("Michael Larabel", syndFeed.entries[0].author)
    }

    @Test
    fun testRealSearchFeedPhoronix() {
        val client = okhttp3.OkHttpClient.Builder()
            .addNetworkInterceptor(me.ash.reader.infrastructure.di.UserAgentInterceptor)
            .build()
        val helper = RssHelper(mockContext, kotlinx.coroutines.Dispatchers.IO, client)
        kotlinx.coroutines.runBlocking {
            val result = helper.searchFeed("https://www.phoronix.com/rss.php")
            Assert.assertNotNull(result.feed)
            Assert.assertEquals("Phoronix", result.feed.title)
            Assert.assertEquals("https://www.phoronix.com/rss.php", result.feedLink)
        }
    }

    @Test
    fun testRealDiscoverFeedPhoronix() {
        val client = okhttp3.OkHttpClient.Builder()
            .addNetworkInterceptor(me.ash.reader.infrastructure.di.UserAgentInterceptor)
            .build()
        val helper = RssHelper(mockContext, kotlinx.coroutines.Dispatchers.IO, client)
        kotlinx.coroutines.runBlocking {
            val result = helper.searchFeed("https://www.phoronix.com")
            Assert.assertNotNull(result.feed)
            Assert.assertEquals("Phoronix", result.feed.title)
            Assert.assertEquals("https://www.phoronix.com/rss.php", result.feedLink)
        }
    }
}

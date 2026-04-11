package me.ash.reader.infrastructure.rss

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.ash.reader.infrastructure.rss.provider.feedly.FeedlyDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies that [FeedlyDTO] field names match the real Feedly API response shapes.
 *
 * Field-name mismatches between DTOs and the actual JSON are the most common
 * integration bug and the easiest to catch with these offline parsing tests.
 * All JSON fixtures are modelled on real Feedly API responses.
 */
class FeedlyDTOTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = Gson()
    }

    // -------------------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------------------

    @Test
    fun `profile parses id and display name fields`() {
        val json = """
            {
              "id": "c805fcbf-3acf-4302-a97e-d82f9d7c897f",
              "email": "user@example.com",
              "givenName": "Jane",
              "familyName": "Doe",
              "fullName": "Jane Doe"
            }
        """.trimIndent()

        val profile = gson.fromJson(json, FeedlyDTO.Profile::class.java)

        assertEquals("c805fcbf-3acf-4302-a97e-d82f9d7c897f", profile.id)
        assertEquals("user@example.com", profile.email)
        assertEquals("Jane", profile.givenName)
        assertEquals("Doe", profile.familyName)
        assertEquals("Jane Doe", profile.fullName)
    }

    @Test
    fun `profile handles missing optional fields`() {
        val json = """{"id": "abc-123"}"""

        val profile = gson.fromJson(json, FeedlyDTO.Profile::class.java)

        assertEquals("abc-123", profile.id)
        assertNull(profile.email)
        assertNull(profile.givenName)
        assertNull(profile.familyName)
        assertNull(profile.fullName)
    }

    // -------------------------------------------------------------------------
    // Subscriptions
    // -------------------------------------------------------------------------

    @Test
    fun `subscription parses id, title, categories and iconUrl`() {
        val json = """
            {
              "id": "feed/http://feeds.example.com/rss",
              "title": "Example Blog",
              "categories": [
                {"id": "user/c805fcbf/category/tech", "label": "Tech"}
              ],
              "iconUrl": "https://storage.googleapis.com/feedly/icons/example.png",
              "website": "https://example.com",
              "updated": 1712345678000
            }
        """.trimIndent()

        val sub = gson.fromJson(json, FeedlyDTO.Subscription::class.java)

        assertEquals("feed/http://feeds.example.com/rss", sub.id)
        assertEquals("Example Blog", sub.title)
        assertEquals(1, sub.categories?.size)
        assertEquals("user/c805fcbf/category/tech", sub.categories?.first()?.id)
        assertEquals("Tech", sub.categories?.first()?.label)
        assertEquals("https://storage.googleapis.com/feedly/icons/example.png", sub.iconUrl)
        assertEquals("https://example.com", sub.website)
        assertEquals(1712345678000L, sub.updated)
    }

    @Test
    fun `subscription with no categories returns null or empty list`() {
        val json = """{"id": "feed/http://feeds.example.com/rss", "title": "Blog"}"""

        val sub = gson.fromJson(json, FeedlyDTO.Subscription::class.java)

        assertTrue(sub.categories.isNullOrEmpty())
    }

    @Test
    fun `subscription list parses array of subscriptions`() {
        val json = """
            [
              {"id": "feed/http://a.com/rss", "title": "A"},
              {"id": "feed/http://b.com/rss", "title": "B"}
            ]
        """.trimIndent()

        val type = object : TypeToken<Array<FeedlyDTO.Subscription>>() {}.type
        val subs = gson.fromJson<Array<FeedlyDTO.Subscription>>(json, type)

        assertEquals(2, subs.size)
        assertEquals("feed/http://a.com/rss", subs[0].id)
        assertEquals("feed/http://b.com/rss", subs[1].id)
    }

    // -------------------------------------------------------------------------
    // Collections
    // -------------------------------------------------------------------------

    @Test
    fun `collection parses id, label and nested subscriptions`() {
        val json = """
            [
              {
                "id": "user/c805fcbf/category/tech",
                "label": "Tech",
                "subscriptions": [
                  {"id": "feed/http://feeds.example.com/rss", "title": "Example Blog"}
                ]
              }
            ]
        """.trimIndent()

        val type = object : TypeToken<Array<FeedlyDTO.Collection>>() {}.type
        val collections = gson.fromJson<Array<FeedlyDTO.Collection>>(json, type)

        assertEquals(1, collections.size)
        val col = collections[0]
        assertEquals("user/c805fcbf/category/tech", col.id)
        assertEquals("Tech", col.label)
        assertEquals(1, col.subscriptions?.size)
        assertEquals("feed/http://feeds.example.com/rss", col.subscriptions?.first()?.id)
    }

    // -------------------------------------------------------------------------
    // StreamContents + StreamItem
    // -------------------------------------------------------------------------

    @Test
    fun `stream contents parses id, continuation and items`() {
        val json = """
            {
              "id": "user/c805fcbf/category/global.all",
              "updated": 1712345678000,
              "continuation": "page2token",
              "items": []
            }
        """.trimIndent()

        val stream = gson.fromJson(json, FeedlyDTO.StreamContents::class.java)

        assertEquals("user/c805fcbf/category/global.all", stream.id)
        assertEquals(1712345678000L, stream.updated)
        assertEquals("page2token", stream.continuation)
        assertEquals(0, stream.items?.size)
    }

    @Test
    fun `stream item parses all article fields`() {
        val json = """
            {
              "id": "tag:google.com,2013:googlealerts/feed:12345",
              "title": "Article Title",
              "author": "Jane Doe",
              "published": 1712345678000,
              "updated": 1712345699000,
              "unread": true,
              "summary": {"content": "<p>Summary text</p>", "direction": "ltr"},
              "content": {"content": "<p>Full article content</p>", "direction": "ltr"},
              "canonical": [{"href": "https://example.com/article", "type": "text/html"}],
              "alternate": [{"href": "https://example.com/alt", "type": "text/html"}],
              "origin": {
                "feedId": "feed/http://feeds.example.com/rss",
                "title": "Example Blog",
                "htmlUrl": "https://example.com"
              },
              "tags": [
                {"id": "user/c805fcbf/tag/global.saved", "label": "saved"}
              ],
              "visual": {"url": "https://example.com/image.jpg", "width": 800, "height": 600},
              "thumbnail": [{"url": "https://example.com/thumb.jpg", "width": 200, "height": 150}]
            }
        """.trimIndent()

        val item = gson.fromJson(json, FeedlyDTO.StreamItem::class.java)

        assertEquals("tag:google.com,2013:googlealerts/feed:12345", item.id)
        assertEquals("Article Title", item.title)
        assertEquals("Jane Doe", item.author)
        assertEquals(1712345678000L, item.published)
        assertEquals(1712345699000L, item.updated)
        assertTrue(item.unread == true)
        assertEquals("<p>Summary text</p>", item.summary?.content)
        assertEquals("<p>Full article content</p>", item.content?.content)
        assertEquals("https://example.com/article", item.canonical?.first()?.href)
        assertEquals("https://example.com/alt", item.alternate?.first()?.href)
        assertEquals("feed/http://feeds.example.com/rss", item.origin?.feedId)
        assertEquals("https://example.com", item.origin?.htmlUrl)
        assertEquals(1, item.tags?.size)
        assertEquals("user/c805fcbf/tag/global.saved", item.tags?.first()?.id)
        assertEquals("https://example.com/image.jpg", item.visual?.url)
        assertEquals("https://example.com/thumb.jpg", item.thumbnail?.first()?.url)
    }

    @Test
    fun `stream item with unread false is not unread`() {
        val json = """{"id": "entry1", "unread": false}"""
        val item = gson.fromJson(json, FeedlyDTO.StreamItem::class.java)
        assertFalse(item.unread == true)
    }

    @Test
    fun `stream item with null unread treated as unread in service logic`() {
        // FeedlyRssService uses: item.unread != false
        // So when unread is null (absent from JSON) the article is treated as unread.
        val json = """{"id": "entry1", "title": "No unread field"}"""
        val item = gson.fromJson(json, FeedlyDTO.StreamItem::class.java)
        assertNull(item.unread)
        assertTrue(item.unread != false) // mirrors the service logic
    }

    @Test
    fun `starred detection uses global saved tag`() {
        // The service checks: tags?.any { it.id?.contains("global.saved") == true }
        val savedTag = FeedlyDTO.Tag(id = "user/abc/tag/global.saved", label = "saved")
        val otherTag = FeedlyDTO.Tag(id = "user/abc/tag/tech", label = "tech")

        assertTrue(savedTag.id?.contains("global.saved") == true)
        assertFalse(otherTag.id?.contains("global.saved") == true)

        val tags = listOf(savedTag, otherTag)
        assertTrue(tags.any { it.id?.contains("global.saved") == true })
    }

    @Test
    fun `starred detection returns false when no tags`() {
        val item = FeedlyDTO.StreamItem(
            id = "entry1", title = null, author = null, published = null,
            updated = null, unread = true, summary = null, content = null,
            canonical = null, alternate = null, origin = null,
            tags = null, enclosure = null, visual = null, thumbnail = null,
        )
        assertFalse(item.tags?.any { it.id?.contains("global.saved") == true } == true)
    }

    @Test
    fun `stream contents with no continuation means last page`() {
        val json = """
            {
              "id": "user/c805fcbf/category/global.all",
              "items": [{"id": "entry1"}]
            }
        """.trimIndent()

        val stream = gson.fromJson(json, FeedlyDTO.StreamContents::class.java)

        assertNull(stream.continuation)
    }

    // -------------------------------------------------------------------------
    // Feed URL extraction
    // -------------------------------------------------------------------------

    @Test
    fun `feed URL is extracted by removing feed prefix`() {
        // FeedlyRssService does: feedId.removePrefix("feed/")
        val feedId = "feed/http://feeds.example.com/rss"
        assertEquals("http://feeds.example.com/rss", feedId.removePrefix("feed/"))
    }

    @Test
    fun `feed URL without prefix is unchanged`() {
        val feedId = "http://feeds.example.com/rss"
        assertEquals("http://feeds.example.com/rss", feedId.removePrefix("feed/"))
    }

    // -------------------------------------------------------------------------
    // MarkersRequest serialisation
    // -------------------------------------------------------------------------

    @Test
    fun `markers request serialises correctly for markAsRead entries`() {
        val request = FeedlyDTO.MarkersRequest(
            action = "markAsRead",
            type = "entries",
            entryIds = listOf("entry1", "entry2"),
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"action\":\"markAsRead\""))
        assertTrue(json.contains("\"type\":\"entries\""))
        assertTrue(json.contains("entry1"))
        assertTrue(json.contains("entry2"))
    }

    @Test
    fun `markers request serialises correctly for markAsSaved`() {
        val request = FeedlyDTO.MarkersRequest(
            action = "markAsSaved",
            type = "entries",
            entryIds = listOf("entry1"),
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"action\":\"markAsSaved\""))
        assertFalse(json.contains("feedIds"))
    }

    @Test
    fun `markers request for feed includes asOf timestamp`() {
        val request = FeedlyDTO.MarkersRequest(
            action = "markAsRead",
            type = "feeds",
            feedIds = listOf("feed/http://example.com/rss"),
            asOf = 1712345678000L,
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("\"type\":\"feeds\""))
        assertTrue(json.contains("1712345678000"))
    }
}

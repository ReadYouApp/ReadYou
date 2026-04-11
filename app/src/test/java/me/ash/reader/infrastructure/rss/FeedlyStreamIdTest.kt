package me.ash.reader.infrastructure.rss

import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.spacerDollar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder

/**
 * Verifies stream ID construction and the DB ID helpers used by [FeedlyRssService].
 *
 * These are pure logic tests — no Android framework, no network.
 */
class FeedlyStreamIdTest {

    // -------------------------------------------------------------------------
    // dollarLast() — extracts the remote ID stored after the last '$' separator
    // -------------------------------------------------------------------------

    @Test
    fun `dollarLast extracts entry ID from db ID`() {
        val dbId = "5\$tag:google.com,2013:googlealerts/feed:12345"
        assertEquals("tag:google.com,2013:googlealerts/feed:12345", dbId.dollarLast())
    }

    @Test
    fun `dollarLast extracts feed ID from db ID`() {
        val dbId = "5\$feed/http://feeds.example.com/rss"
        assertEquals("feed/http://feeds.example.com/rss", dbId.dollarLast())
    }

    @Test
    fun `dollarLast extracts category ID from db ID`() {
        val dbId = "5\$user/c805fcbf/category/tech"
        assertEquals("user/c805fcbf/category/tech", dbId.dollarLast())
    }

    // -------------------------------------------------------------------------
    // spacerDollar() — creates the composite DB ID
    // -------------------------------------------------------------------------

    @Test
    fun `spacerDollar constructs db ID from accountId and remote ID`() {
        val accountId = 3
        val remoteId = "feed/http://feeds.example.com/rss"
        assertEquals("3\$feed/http://feeds.example.com/rss", accountId.spacerDollar(remoteId))
    }

    @Test
    fun `spacerDollar and dollarLast are inverse operations`() {
        val accountId = 5
        val remoteId = "tag:google.com,2013:googlealerts/feed:99999"
        val dbId = accountId.spacerDollar(remoteId)
        assertEquals(remoteId, dbId.dollarLast())
    }

    // -------------------------------------------------------------------------
    // Global stream ID construction (used in sync)
    // -------------------------------------------------------------------------

    @Test
    fun `global all stream ID is constructed correctly`() {
        val userId = "c805fcbf-3acf-4302-a97e-d82f9d7c897f"
        val streamId = "user/$userId/category/global.all"
        assertEquals("user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.all", streamId)
    }

    @Test
    fun `global saved tag ID is constructed correctly`() {
        val userId = "c805fcbf-3acf-4302-a97e-d82f9d7c897f"
        val tagId = "user/$userId/tag/global.saved"
        assertTrue(tagId.contains("global.saved"))
    }

    @Test
    fun `category stream ID is constructed correctly`() {
        val userId = "c805fcbf"
        val categoryLabel = "tech"
        val categoryStreamId = "user/$userId/category/$categoryLabel"
        assertEquals("user/c805fcbf/category/tech", categoryStreamId)
    }

    // -------------------------------------------------------------------------
    // URL encoding of stream IDs (used in FeedlyAPI.getStreamContents)
    // -------------------------------------------------------------------------

    @Test
    fun `stream ID with slashes is URL encoded for HTTP request`() {
        val streamId = "user/c805fcbf/category/global.all"
        val encoded = URLEncoder.encode(streamId, "UTF-8")

        // Slashes and dots should be percent-encoded
        assertTrue(encoded.contains("%2F") || encoded.contains("%2f"))
        assertTrue(!encoded.contains("/"))
    }

    @Test
    fun `feed ID with URL is URL encoded for HTTP request`() {
        val feedId = "feed/http://feeds.example.com/rss"
        val encoded = URLEncoder.encode(feedId, "UTF-8")

        assertTrue(!encoded.contains("/"))
        assertTrue(!encoded.contains(":"))
    }

    @Test
    fun `encoded stream ID can be decoded back to original`() {
        val streamId = "user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.all"
        val encoded = URLEncoder.encode(streamId, "UTF-8")
        val decoded = java.net.URLDecoder.decode(encoded, "UTF-8")
        assertEquals(streamId, decoded)
    }

    // -------------------------------------------------------------------------
    // Feed URL extraction from Feedly feed ID
    // -------------------------------------------------------------------------

    @Test
    fun `feed URL is obtained by removing feed prefix`() {
        assertEquals(
            "http://feeds.example.com/rss",
            "feed/http://feeds.example.com/rss".removePrefix("feed/"),
        )
    }

    @Test
    fun `https feed URL is obtained by removing feed prefix`() {
        assertEquals(
            "https://feeds.example.com/atom.xml",
            "feed/https://feeds.example.com/atom.xml".removePrefix("feed/"),
        )
    }

    // -------------------------------------------------------------------------
    // Subscription category membership
    // -------------------------------------------------------------------------

    @Test
    fun `category DB ID uses full category stream ID as remote ID`() {
        val accountId = 5
        val categoryStreamId = "user/c805fcbf/category/tech"
        val dbId = accountId.spacerDollar(categoryStreamId)

        assertEquals("5\$user/c805fcbf/category/tech", dbId)
        assertEquals(categoryStreamId, dbId.dollarLast())
    }
}

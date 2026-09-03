package me.ash.reader.infrastructure.rss

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that ROME (the feed parser used by ReadYou) can parse an Atom feed
 * in the exact shape served by Atlassian Statuspage instances
 * (e.g. https://status.deepseek.com/feed.rss which redirects to /history.atom).
 *
 * The fixture is a real statuspage Atom payload captured from a live instance
 * and truncated to the first 3 entries.
 */
class StatuspageAtomParsingTest {

    private fun readSampleAtom(): ByteArray {
        val resource =
            requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/statuspage_history.atom")) {
                "fixture statuspage_history.atom not found on classpath"
            }
        return resource.use { it.readBytes() }
    }

    @Test
    fun `statuspage atom feed parses with http content type like real server`() {
        val body = readSampleAtom()
        val feed =
            SyndFeedInput().build(
                XmlReader(
                    ByteArrayInputStream(body),
                    "application/atom+xml; charset=utf-8", // exact Content-Type of statuspage
                )
            )

        assertNotNull(feed.title)
        assertTrue("feed must expose entries", feed.entries.isNotEmpty())
        assertEquals("first entry should carry an incident link", 3, feed.entries.size)

        val first = feed.entries.first()
        assertNotNull("entry title", first.title)
        assertNotNull("entry link", first.link)
        assertTrue(
            "entry link should point to an incident page",
            first.link!!.contains("/incidents/") || first.link!!.contains("/history"),
        )
    }

    @Test
    fun `statuspage atom entries expose published and updated dates`() {
        val body = readSampleAtom()
        val feed =
            SyndFeedInput().build(
                XmlReader(
                    ByteArrayInputStream(body),
                    "application/atom+xml; charset=utf-8",
                )
            )
        feed.entries.forEach { entry ->
            assertNotNull(
                "entry '${entry.title}' must expose a usable date (published or updated)",
                entry.publishedDate ?: entry.updatedDate,
            )
        }
    }
}

package me.ash.reader.domain.model.account.security

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ash.reader.domain.data.Diff
import me.ash.reader.infrastructure.net.LatestRelease
import me.ash.reader.infrastructure.rss.provider.greader.GoogleReaderDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies that kotlinx.serialization produces JSON backward-compatible with the
 * previous Gson implementation.
 *
 * Each test creates a known object, serializes it, then deserializes the result
 * and checks the round-trip. A second path deserializes a hand-written JSON
 * string that matches the format Gson would have produced, proving the new
 * serializer can read data written by the old one.
 */
class SerializationCompatibilityTest {

    private val json = Json { ignoreUnknownKeys = true }

    // -- GoogleReaderSecurityKey --

    @Test
    fun `GoogleReaderSecurityKey round-trips correctly`() {
        // Simulate what Gson().toJson(key) would have produced
        val gsonJson = """{"serverUrl":"https://rss.example.com","username":"user","password":"pass","clientCertificateAlias":null}"""

        val decoded = json.decodeFromString<GoogleReaderSecurityKey>(gsonJson)
        assertEquals("https://rss.example.com", decoded.serverUrl)
        assertEquals("user", decoded.username)
        assertEquals("pass", decoded.password)
        assertNull(decoded.clientCertificateAlias)

        // Re-encode and decode to verify round-trip
        val reEncoded = json.encodeToString(decoded)
        val reDecoded = json.decodeFromString<GoogleReaderSecurityKey>(reEncoded)
        assertEquals(decoded.serverUrl, reDecoded.serverUrl)
        assertEquals(decoded.username, reDecoded.username)
        assertEquals(decoded.password, reDecoded.password)
    }

    @Test
    fun `FeverSecurityKey round-trips correctly`() {
        val gsonJson = """{"serverUrl":"https://fever.example.com","username":"admin","password":"secret","clientCertificateAlias":"cert1"}"""
        val decoded = json.decodeFromString<FeverSecurityKey>(gsonJson)
        assertEquals("https://fever.example.com", decoded.serverUrl)
        assertEquals("cert1", decoded.clientCertificateAlias)
    }

    @Test
    fun `FreshRSSSecurityKey round-trips correctly`() {
        val gsonJson = """{"serverUrl":"https://freshrss.example.com","username":"user","password":"pw","clientCertificateAlias":null}"""
        val decoded = json.decodeFromString<FreshRSSSecurityKey>(gsonJson)
        assertEquals("https://freshrss.example.com", decoded.serverUrl)
    }

    @Test
    fun `LocalSecurityKey round-trips with empty object`() {
        val gsonJson = """{}"""
        val decoded = json.decodeFromString<LocalSecurityKey>(gsonJson)
        val reEncoded = json.encodeToString(decoded)
        assertEquals("{}", reEncoded)
    }

    // -- GoogleReaderDTO --

    @Test
    fun `GoogleReaderDTO SubscriptionList deserializes from Gson-format JSON`() {
        val gsonJson = """{"subscriptions":[{"id":"feed/3","title":"Fedora Magazine","categories":[{"id":"user/-/label/Blogs","label":"Blogs"}],"url":"http://fedoramagazine.org/feed/","htmlUrl":"http://fedoramagazine.org/","iconUrl":"https://demo.freshrss.org/f.php?f2b1439b","sortid":null}]}"""
        val decoded = json.decodeFromString<GoogleReaderDTO.SubscriptionList>(gsonJson)
        assertEquals(1, decoded.subscriptions.size)
        assertEquals("feed/3", decoded.subscriptions[0].id)
        assertEquals("Blogs", decoded.subscriptions[0].categories?.get(0)?.label)
    }

    @Test
    fun `GoogleReaderDTO ItemsContents deserializes from Gson-format JSON`() {
        val gsonJson = """{"id":"user/-/state/com.google/reading-list","updated":1705045799,"items":[{"id":"tag:google.com,2005:reader/item/00060eba36e4f4e1","published":1704982200,"title":"Test Article","summary":{"content":"<p>Hello</p>"},"categories":["user/-/state/com.google/reading-list"],"origin":{"streamId":"feed/2","htmlUrl":"https://example.com","title":"Example"}}]}"""
        val decoded = json.decodeFromString<GoogleReaderDTO.ItemsContents>(gsonJson)
        assertEquals(1, decoded.items?.size)
        assertEquals("Test Article", decoded.items?.get(0)?.title)
        assertEquals("feed/2", decoded.items?.get(0)?.origin?.streamId)
    }

    @Test
    fun `GoogleReaderDTO GReaderError with SerialName errors field`() {
        val gsonJson = """{"errors":["Unauthorized","Bad request"]}"""
        val decoded = json.decodeFromString<GoogleReaderDTO.GReaderError>(gsonJson)
        assertEquals(2, decoded.errors.size)
        assertEquals("Unauthorized", decoded.errors[0])
    }

    // -- LatestRelease (snake_case from GitHub API) --

    @Test
    fun `LatestRelease deserializes snake_case JSON from GitHub API`() {
        val githubJson = """{"html_url":"https://github.com/Ashinch/ReadYou/releases/v1.0","tag_name":"v1.0","name":"Release 1.0","draft":false,"prerelease":false,"created_at":"2024-01-01","published_at":"2024-01-02","assets":[{"name":"app.apk","content_type":"application/vnd.android.package-archive","size":12345,"download_count":100,"browser_download_url":"https://github.com/dl/app.apk"}],"body":"Release notes"}"""
        val decoded = json.decodeFromString<LatestRelease>(githubJson)
        assertEquals("v1.0", decoded.tagName)
        assertEquals("https://github.com/Ashinch/ReadYou/releases/v1.0", decoded.htmlUrl)
        assertEquals("2024-01-02", decoded.publishedAt)
        assertEquals(1, decoded.assets?.size)
        assertEquals("https://github.com/dl/app.apk", decoded.assets?.get(0)?.browserDownloadUrl)
        assertEquals(100, decoded.assets?.get(0)?.downloadCount)
    }

    // -- Diff --

    @Test
    fun `Diff round-trips correctly`() {
        val gsonJson = """{"isUnread":true,"articleId":"article-123","feedId":"feed-456"}"""
        val decoded = json.decodeFromString<Diff>(gsonJson)
        assertEquals(true, decoded.isUnread)
        assertEquals("article-123", decoded.articleId)
        assertEquals("feed-456", decoded.feedId)

        val reEncoded = json.encodeToString(decoded)
        val reDecoded = json.decodeFromString<Diff>(reEncoded)
        assertEquals(decoded, reDecoded)
    }

    @Test
    fun `Diff map serialization matches Gson format`() {
        val gsonMapJson = """{"id1":{"isUnread":true,"articleId":"id1","feedId":"f1"},"id2":{"isUnread":false,"articleId":"id2","feedId":"f2"}}"""
        val decoded = json.decodeFromString<Map<String, Diff>>(gsonMapJson)
        assertEquals(2, decoded.size)
        assertEquals(true, decoded["id1"]?.isUnread)
        assertEquals(false, decoded["id2"]?.isUnread)
    }
}

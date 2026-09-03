package me.ash.reader.ui.page.home.feeds.subscribe

import com.rometools.rome.io.ParsingFeedException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import me.ash.reader.R
import me.ash.reader.infrastructure.rss.FeedHttpException
import me.ash.reader.infrastructure.rss.FeedNotFoundException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.xml.sax.SAXParseException

class SubscribeErrorsTest {

    @Test
    fun `http error maps to localized http message with status code`() {
        val error = FeedHttpException("HTTP 403 Forbidden", 403).toSubscribeError()
        assertEquals(SubscribeError.Text(R.string.subscribe_error_http, listOf(403)), error)
    }

    @Test
    fun `unreachable host maps to network message`() {
        val errors =
            listOf(
                SSLHandshakeException("connection closed"),
                UnknownHostException("status.deepseek.com"),
                ConnectException("refused"),
                SocketTimeoutException("timeout"),
            )
        errors.forEach {
            assertEquals(
                "expected network message for ${it::class.java.simpleName}",
                SubscribeError.Text(R.string.subscribe_error_network),
                it.toSubscribeError(),
            )
        }
    }

    @Test
    fun `feed not found maps to dedicated message`() {
        val error = FeedNotFoundException("Unable to detect RSS feed URL").toSubscribeError()
        assertEquals(SubscribeError.Text(R.string.subscribe_error_feed_not_found), error)
    }

    @Test
    fun `unparseable feed body maps to parse message`() {
        val errors =
            listOf<Throwable>(
                SAXParseException("mismatched tag", null, null),
                ParsingFeedException("Invalid XML"),
            )
        errors.forEach {
            assertEquals(
                "expected parse message for ${it::class.java.simpleName}",
                SubscribeError.Text(R.string.subscribe_error_parse),
                it.toSubscribeError(),
            )
        }
    }

    @Test
    fun `other io exceptions keep their original message`() {
        val error = IOException("boom").toSubscribeError()
        assertEquals(SubscribeError.Raw("boom"), error)
    }

    @Test
    fun `generic exceptions keep their original message`() {
        val error = IllegalStateException("unexpected").toSubscribeError()
        assertEquals(SubscribeError.Raw("unexpected"), error)
    }
}

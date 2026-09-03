package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.annotation.StringRes
import com.rometools.rome.io.FeedException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import me.ash.reader.R
import me.ash.reader.infrastructure.rss.FeedHttpException
import me.ash.reader.infrastructure.rss.FeedNotFoundException
import org.xml.sax.SAXParseException

/**
 * Maps an exception raised while importing a feed through
 * [me.ash.reader.infrastructure.rss.RssHelper.searchFeed] to a user-facing
 * error message.
 *
 * Import failures fall into a few categories that the user can actually act on:
 * - the host is unreachable or TLS is blocked (e.g. a status page that is
 *   geo-blocked or behind an ICP firewall) -> [R.string.subscribe_error_network]
 * - the server answered with an error status -> [R.string.subscribe_error_http]
 * - the URL is not a feed and no feed could be discovered
 *   -> [R.string.subscribe_error_feed_not_found]
 * - the body could not be parsed as RSS/Atom -> [R.string.subscribe_error_parse]
 *
 * Anything else keeps its original message ([SubscribeError.Raw]) so that no
 * debugging information is lost.
 */
fun Throwable.toSubscribeError(): SubscribeError =
    when (this) {
        is FeedHttpException -> SubscribeError.Text(R.string.subscribe_error_http, listOf(statusCode))
        is FeedNotFoundException -> SubscribeError.Text(R.string.subscribe_error_feed_not_found)
        is SSLException,
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException,
        -> SubscribeError.Text(R.string.subscribe_error_network)
        is SAXParseException, is FeedException,
        -> SubscribeError.Text(R.string.subscribe_error_parse)
        else -> SubscribeError.Raw(message.orEmpty())
    }

sealed interface SubscribeError {
    /** A localized message referenced by a string resource. */
    data class Text(@StringRes val resId: Int, val formatArgs: List<Any> = emptyList()) : SubscribeError

    /** The raw exception message; falls back to [R.string.subscribe_error_fallback] when empty. */
    data class Raw(val message: String) : SubscribeError
}

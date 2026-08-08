package me.ash.reader.infrastructure.rss.provider.miniflux

import com.google.gson.annotations.SerializedName

object MinifluxDTO {

    data class User(
        @SerializedName("id") val id: Long,
        @SerializedName("username") val username: String,
        @SerializedName("is_admin") val isAdmin: Boolean? = false,
        @SerializedName("theme") val theme: String? = null,
        @SerializedName("language") val language: String? = null,
        @SerializedName("timezone") val timezone: String? = null,
    )

    data class Category(
        @SerializedName("id") val id: Long,
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("title") val title: String,
        @SerializedName("hide_globally") val hideGlobally: Boolean? = false,
        @SerializedName("feed_count") val feedCount: Int? = null,
        @SerializedName("total_unread") val totalUnread: Int? = null,
    )

    data class CreateCategoryRequest(
        @SerializedName("title") val title: String,
        @SerializedName("hide_globally") val hideGlobally: Boolean? = null,
    )

    data class UpdateCategoryRequest(
        @SerializedName("title") val title: String? = null,
        @SerializedName("hide_globally") val hideGlobally: Boolean? = null,
    )

    data class FeedIcon(
        @SerializedName("feed_id") val feedId: Long,
        @SerializedName("icon_id") val iconId: Long,
    )

    data class Icon(
        @SerializedName("id") val id: Long,
        @SerializedName("data") val data: String,
        @SerializedName("mime_type") val mimeType: String,
    )

    data class Feed(
        @SerializedName("id") val id: Long,
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("title") val title: String,
        @SerializedName("site_url") val siteUrl: String? = null,
        @SerializedName("feed_url") val feedUrl: String,
        @SerializedName("checked_at") val checkedAt: String? = null,
        @SerializedName("parsing_error_message") val parsingErrorMessage: String? = null,
        @SerializedName("parsing_error_count") val parsingErrorCount: Int? = 0,
        @SerializedName("disabled") val disabled: Boolean? = false,
        @SerializedName("category") val category: Category? = null,
        @SerializedName("icon") val icon: FeedIcon? = null,
    )

    data class CreateFeedRequest(
        @SerializedName("feed_url") val feedUrl: String,
        @SerializedName("category_id") val categoryId: Long? = null,
        @SerializedName("username") val username: String? = null,
        @SerializedName("password") val password: String? = null,
        @SerializedName("crawler") val crawler: Boolean? = null,
        @SerializedName("user_agent") val userAgent: String? = null,
    )

    data class CreateFeedResponse(
        @SerializedName("feed_id") val feedId: Long,
    )

    data class UpdateFeedRequest(
        @SerializedName("title") val title: String? = null,
        @SerializedName("category_id") val categoryId: Long? = null,
        @SerializedName("feed_url") val feedUrl: String? = null,
        @SerializedName("site_url") val siteUrl: String? = null,
    )

    data class Enclosure(
        @SerializedName("id") val id: Long? = null,
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("entry_id") val entryId: Long? = null,
        @SerializedName("url") val url: String? = null,
        @SerializedName("mime_type") val mimeType: String? = null,
        @SerializedName("size") val size: Long? = null,
    )

    data class Entry(
        @SerializedName("id") val id: Long,
        @SerializedName("user_id") val userId: Long? = null,
        @SerializedName("feed_id") val feedId: Long,
        @SerializedName("status") val status: String,
        @SerializedName("title") val title: String,
        @SerializedName("url") val url: String,
        @SerializedName("comments_url") val commentsUrl: String? = null,
        @SerializedName("author") val author: String? = null,
        @SerializedName("content") val content: String? = null,
        @SerializedName("hash") val hash: String? = null,
        @SerializedName("published_at") val publishedAt: String,
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("starred") val starred: Boolean = false,
        @SerializedName("reading_time") val readingTime: Int? = null,
        @SerializedName("enclosures") val enclosures: List<Enclosure>? = null,
        @SerializedName("feed") val feed: Feed? = null,
    )

    data class EntriesResponse(
        @SerializedName("total") val total: Int,
        @SerializedName("entries") val entries: List<Entry>,
    )

    data class EntryIDsResponse(
        @SerializedName("total") val total: Int,
        @SerializedName("entry_ids") val entryIds: List<Long>,
    )

    data class UpdateEntriesRequest(
        @SerializedName("entry_ids") val entryIds: List<Long>,
        @SerializedName("status") val status: String? = null,
        @SerializedName("starred") val starred: Boolean? = null,
    )

    data class DiscoverSubscription(
        @SerializedName("url") val url: String,
        @SerializedName("title") val title: String,
        @SerializedName("type") val type: String,
    )

    data class DiscoverRequest(
        @SerializedName("url") val url: String,
    )

    data class ErrorResponse(
        @SerializedName("error_message") val errorMessage: String? = null,
    )
}

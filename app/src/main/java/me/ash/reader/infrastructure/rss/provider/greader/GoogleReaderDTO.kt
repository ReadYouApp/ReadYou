package me.ash.reader.infrastructure.rss.provider.greader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object GoogleReaderDTO {

    @Serializable
    data class GReaderError(
        @SerialName("errors") val errors: List<String>,
    )

    @Serializable
    data class MinifluxAuthData(
        val SID: String? = null,
        val LSID: String? = null,
        val Auth: String? = null,
    )

    @Serializable
    data class User(
        val userId: String? = null,
        val userName: String? = null,
        val userProfileId: String? = null,
        val userEmail: String? = null,
    )

    @Serializable
    data class SubscriptionList(
        val subscriptions: List<Feed> = emptyList(),
    )

    @Serializable
    data class Feed(
        val id: String? = null,
        val title: String? = null,
        val categories: List<Category>? = null,
        val url: String? = null,
        val htmlUrl: String? = null,
        val iconUrl: String? = null,
        val sortid: String? = null,
    )

    @Serializable
    data class Category(
        val id: String? = null,
        val label: String? = null,
    )

    @Serializable
    data class QuickAddFeed(
        val numResults: Long? = null,
        val query: String? = null,
        val streamId: String? = null,
        val streamName: String? = null,
    )

    @Serializable
    data class ItemIds(
        val itemRefs: List<Item>? = null,
        val continuation: String? = null,
    )

    @Serializable
    data class ItemsContents(
        val id: String? = null,
        val updated: Long? = null,
        val items: List<Item>? = null,
    )

    @Serializable
    data class Item(
        val id: String? = null,
        val crawlTimeMsec: String? = null,
        val timestampUsec: String? = null,
        val published: Long? = null,
        val title: String? = null,
        val summary: Summary? = null,
        val categories: List<String>? = null,
        val origin: OriginItem? = null,
        val author: String? = null,
        val canonical: List<CanonicalItem>? = null,
        val alternate: List<CanonicalItem>? = null,
    )

    @Serializable
    data class Summary(
        val content: String? = null,
    )

    @Serializable
    data class CanonicalItem(
        val href: String? = null,
    )

    @Serializable
    data class OriginItem(
        val streamId: String? = null,
        val htmlUrl: String? = null,
        val title: String? = null,
    )
}

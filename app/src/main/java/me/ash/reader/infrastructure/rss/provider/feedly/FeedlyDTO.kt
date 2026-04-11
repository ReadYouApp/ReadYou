package me.ash.reader.infrastructure.rss.provider.feedly

object FeedlyDTO {

    data class Profile(
        val id: String?,
        val email: String?,
        val givenName: String?,
        val familyName: String?,
        val fullName: String?,
    )

    data class Category(
        val id: String?,
        val label: String?,
    )

    data class Subscription(
        val id: String?,
        val title: String?,
        val categories: List<Category>?,
        val iconUrl: String?,
        val website: String?,
        val updated: Long?,
    )

    data class Collection(
        val id: String?,
        val label: String?,
        val subscriptions: List<Subscription>?,
    )

    data class Tag(
        val id: String?,
        val label: String?,
    )

    data class Content(
        val content: String?,
        val direction: String?,
    )

    data class Link(
        val href: String?,
        val type: String?,
    )

    data class Visual(
        val url: String?,
        val width: Int?,
        val height: Int?,
    )

    data class Origin(
        val feedId: String?,
        val title: String?,
        val htmlUrl: String?,
    )

    data class StreamItem(
        val id: String?,
        val title: String?,
        val author: String?,
        val published: Long?,
        val updated: Long?,
        val unread: Boolean?,
        val summary: Content?,
        val content: Content?,
        val canonical: List<Link>?,
        val alternate: List<Link>?,
        val origin: Origin?,
        val tags: List<Tag>?,
        val enclosure: List<Link>?,
        val visual: Visual?,
        val thumbnail: List<Visual>?,
    )

    data class StreamContents(
        val id: String?,
        val items: List<StreamItem>?,
        val continuation: String?,
        val updated: Long?,
    )

    data class MarkersRequest(
        val action: String,
        val type: String,
        val entryIds: List<String>? = null,
        val feedIds: List<String>? = null,
        val categoryIds: List<String>? = null,
        val asOf: Long? = null,
    )
}

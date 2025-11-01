package me.ash.reader.domain.model.group

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feedgroup.FeedGroup

/**
 * A [group] contains many [feeds] via many-to-many relationship through [FeedGroup] junction table.
 *
 * This differs from [GroupWithFeed] which uses the legacy one-to-many relationship via Feed.groupId.
 * Use this class for queries that need to support feeds belonging to multiple groups.
 */
data class GroupWithFeedViaJunction(
    @Embedded
    val group: Group,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FeedGroup::class,
            parentColumn = "groupId",
            entityColumn = "feedId"
        )
    )
    val feeds: List<Feed>,
)

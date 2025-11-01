package me.ash.reader.domain.model.feed

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import me.ash.reader.domain.model.feedgroup.FeedGroup
import me.ash.reader.domain.model.group.Group

/**
 * A [feed] belongs to many [groups] via many-to-many relationship through [FeedGroup] junction table.
 *
 * This supports the use case where a single feed can be organized into multiple groups/folders.
 */
data class FeedWithGroups(
    @Embedded
    val feed: Feed,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FeedGroup::class,
            parentColumn = "feedId",
            entityColumn = "groupId"
        )
    )
    val groups: List<Group>,
)

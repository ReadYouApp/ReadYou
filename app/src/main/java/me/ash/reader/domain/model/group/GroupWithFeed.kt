package me.ash.reader.domain.model.group

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.domain.model.feed.Feed

/**
 * A [group] contains many [feeds].
 */
data class GroupWithFeed(
    @Embedded
    val group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val feeds: MutableList<Feed>,
)

package me.ash.reader.domain.model.group

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithKeywordFilter

/**
 * A [group] contains many [feeds], each of which has [keyword_filters].
 */
data class GroupWithFeedWithKeywordFilters(
    @Embedded
    val group: Group,
    @Relation(parentColumn = "id", entityColumn = "groupId")
    val feedsWithKeywordFilters: MutableList<FeedWithKeywordFilter>,
)

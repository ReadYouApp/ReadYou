package me.ash.reader.domain.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import me.ash.reader.domain.model.group.Group

/**
 * A [feed] with filtered keywords.
 */
data class FeedWithKeywordFilter(
    @Embedded
    var feed: Feed,
    @Relation(parentColumn = "feedId", entityColumn = "id")
    var keywordFilters: List<KeywordFilter>,
)

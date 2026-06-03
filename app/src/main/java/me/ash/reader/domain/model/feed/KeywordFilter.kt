package me.ash.reader.domain.model.feed

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * TODO: Add class description
 */
@Entity(
    tableName = "keyword_filter",
    primaryKeys = ["feedId", "keyword"],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class KeywordFilter(
    val feedId: String,
    val keyword: String,
)
package me.ash.reader.domain.model.feed

import androidx.room.*
import me.ash.reader.domain.model.group.Group

/**
 * TODO: Add class description
 */
@Entity(
    tableName = "feed",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )],
)
data class Feed(
    @PrimaryKey
    val id: String,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val icon: String? = null,
    @ColumnInfo
    val url: String,
    @ColumnInfo(index = true)
    var groupId: String,
    @ColumnInfo(index = true)
    val accountId: Int,
    @ColumnInfo
    val isNotification: Boolean = false,
    @ColumnInfo
    val isFullContent: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isBrowser: Boolean = false,
    @Ignore val important: Int = 0
) {
    constructor(
        id: String,
        name: String,
        icon: String?,
        url: String,
        groupId: String,
        accountId: Int,
        isNotification: Boolean,
        isFullContent: Boolean,
        isBrowser: Boolean
    ) : this(
        id = id,
        name = name,
        icon = icon,
        url = url,
        groupId = groupId,
        accountId = accountId,
        isNotification = isNotification,
        isFullContent = isFullContent,
        isBrowser = isBrowser,
        important = 0
    )
}

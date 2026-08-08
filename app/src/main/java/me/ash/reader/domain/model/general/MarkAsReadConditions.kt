package me.ash.reader.domain.model.general

import java.util.Calendar
import java.util.Date

/**
 * Mark as read conditions.
 *
 * - [SevenDays]: Mark as read if more than 7 days old
 * - [ThreeDays]: Mark as read if more than 3 days old
 * - [OneDay]: Mark as read if more than 1 day old
 * - [All]: Mark all as read
 */
enum class MarkAsReadConditions {

    SevenDays,
    ThreeDays,
    OneDay,
    All,
    ;

    fun toDate(): Date? = when (this) {
        All -> null
        else -> Calendar.getInstance().apply {
            time = Date()
            add(Calendar.DAY_OF_MONTH, when (this@MarkAsReadConditions) {
                SevenDays -> -7
                ThreeDays -> -3
                OneDay -> -1
                else -> throw IllegalArgumentException("Unknown condition: $this")
            })
        }.time
    }
}

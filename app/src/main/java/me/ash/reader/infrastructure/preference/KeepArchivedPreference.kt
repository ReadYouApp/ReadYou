package me.ash.reader.infrastructure.preference

import android.content.Context
import me.ash.reader.R
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

sealed class KeepArchivedPreference(
    val value: Long,
) {

    object Always : KeepArchivedPreference(0L)
    object For1Day : KeepArchivedPreference(86400000L)
    object For2Days : KeepArchivedPreference(172800000L)
    object For3Days : KeepArchivedPreference(259200000L)
    object For1Week : KeepArchivedPreference(604800000L)
    object For2Weeks : KeepArchivedPreference(1209600000L)
    object For1Month : KeepArchivedPreference(2592000000L)

    fun put(accountId: Int, viewModel: AccountViewModel) {
        viewModel.update(accountId) { copy(keepArchived = this@KeepArchivedPreference) }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Always -> context.getString(R.string.always)
            For1Day -> context.getString(R.string.for_1_day)
            For2Days -> context.getString(R.string.for_2_days)
            For3Days -> context.getString(R.string.for_3_days)
            For1Week -> context.getString(R.string.for_1_week)
            For2Weeks -> context.getString(R.string.for_2_weeks)
            For1Month -> context.getString(R.string.for_1_month)
        }

    companion object {

        val default = For1Month
        val values = listOf(
            Always,
            For1Day,
            For2Days,
            For3Days,
            For1Week,
            For2Weeks,
            For1Month,
        )
    }
}

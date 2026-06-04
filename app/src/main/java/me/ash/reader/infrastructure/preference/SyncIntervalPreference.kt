package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import me.ash.reader.R
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.ui.page.settings.accounts.AccountViewModel
import java.util.concurrent.TimeUnit

sealed class SyncIntervalPreference(
    val value: Long,
) {

    object Manually : SyncIntervalPreference(0L)
    object Every15Minutes : SyncIntervalPreference(15L)
    object Every30Minutes : SyncIntervalPreference(30L)
    object Every1Hour : SyncIntervalPreference(60L)
    object Every2Hours : SyncIntervalPreference(120L)
    object Every3Hours : SyncIntervalPreference(180L)
    object Every6Hours : SyncIntervalPreference(360L)
    object Every12Hours : SyncIntervalPreference(720L)
    object Every1Day : SyncIntervalPreference(1440L)

    fun put(accountId: Int, viewModel: AccountViewModel) {
        viewModel.update(accountId) { copy(syncInterval = this@SyncIntervalPreference) }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Manually -> context.getString(R.string.manually)
            Every15Minutes -> context.getString(R.string.every_15_minutes)
            Every30Minutes -> context.getString(R.string.every_30_minutes)
            Every1Hour -> context.getString(R.string.every_1_hour)
            Every2Hours -> context.getString(R.string.every_2_hours)
            Every3Hours -> context.getString(R.string.every_3_hours)
            Every6Hours -> context.getString(R.string.every_6_hours)
            Every12Hours -> context.getString(R.string.every_12_hours)
            Every1Day -> context.getString(R.string.every_1_day)
        }

    fun toPeriodicWorkRequestBuilder(): PeriodicWorkRequest.Builder =
        PeriodicWorkRequestBuilder<SyncWorker>(value, TimeUnit.MINUTES)

    companion object {

        val default = Every30Minutes
        val values = listOf(
            Manually,
            Every15Minutes,
            Every30Minutes,
            Every1Hour,
            Every2Hours,
            Every3Hours,
            Every6Hours,
            Every12Hours,
            Every1Day,
        )
    }
}

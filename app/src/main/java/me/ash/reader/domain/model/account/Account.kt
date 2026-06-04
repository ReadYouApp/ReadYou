package me.ash.reader.domain.model.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.ash.reader.domain.model.account.security.DESUtils
import me.ash.reader.infrastructure.preference.*
import java.util.*

/**
 * In the application, at least one account exists and different accounts
 * can have the same feeds and articles.
 */
@Entity(tableName = "account")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val type: AccountType,
    @ColumnInfo
    val updateAt: Date? = null,
    @ColumnInfo
    val lastArticleId: String? = null,
    @ColumnInfo(defaultValue = "30")
    val syncInterval: SyncIntervalPreference = SyncIntervalPreference.default,
    @ColumnInfo(defaultValue = "0")
    val syncOnStart: SyncOnStartPreference = SyncOnStartPreference.default,
    @ColumnInfo(defaultValue = "0")
    val syncOnlyOnWiFi: SyncOnlyOnWiFiPreference = SyncOnlyOnWiFiPreference.default,
    @ColumnInfo(defaultValue = "0")
    val syncOnlyWhenCharging: SyncOnlyWhenChargingPreference = SyncOnlyWhenChargingPreference.default,
    @ColumnInfo(defaultValue = "2592000000")
    val keepArchived: KeepArchivedPreference = KeepArchivedPreference.default,
    @ColumnInfo(defaultValue = "")
    val syncBlockList: SyncBlockList = SyncBlockListPreference.default,
    @ColumnInfo(defaultValue = DESUtils.empty)
    val securityKey: String? = DESUtils.empty,
)

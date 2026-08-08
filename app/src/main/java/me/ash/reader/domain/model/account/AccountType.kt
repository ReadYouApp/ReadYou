package me.ash.reader.domain.model.account

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.painterResource
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.R

/**
 * Each account will specify its local or third-party API type.
 */
class AccountType(val id: Int) {

    /**
     * Make sure the constructed object is valid.
     */
    init {
        if (id < 1 || id > 7) {
            throw IllegalArgumentException("Account type id is not valid.")
        }
    }

    fun toDesc(context: Context): String =
        when (this.id) {
            1 -> context.getString(R.string.local)
            2 -> context.getString(R.string.fever)
            3 -> context.getString(R.string.google_reader)
            4 -> context.getString(R.string.fresh_rss)
            5 -> context.getString(R.string.feedly)
            6 -> context.getString(R.string.inoreader)
            7 -> "Miniflux"
            else -> context.getString(R.string.unknown)
        }

    @Stable
    @Composable
    fun toIcon(): Any =
        when (this.id) {
            1 -> Icons.Rounded.RssFeed
            2 -> painterResource(id = R.drawable.ic_fever)
            3 -> Icons.Rounded.RssFeed
            4 -> painterResource(id = R.drawable.ic_freshrss)
            5 -> painterResource(id = R.drawable.ic_feedly)
            6 -> painterResource(id = R.drawable.ic_inoreader)
            7 -> Icons.Rounded.RssFeed
            else -> Icons.Rounded.RssFeed
        }

    /**
     * Type of account currently supported.
     */
    companion object {

        val Local = AccountType(1)
        val Fever = AccountType(2)
        val GoogleReader = AccountType(3)
        val FreshRSS = AccountType(4)
        val Feedly = AccountType(5)
        val Inoreader = AccountType(6)
        val Miniflux = AccountType(7)
    }
}

/**
 * Provide [TypeConverter] of [AccountType] for [RoomDatabase].
 */
class AccountTypeConverters {

    @TypeConverter
    fun toAccountType(id: Int): AccountType {
        return AccountType(id)
    }

    @TypeConverter
    fun fromAccountType(accountType: AccountType): Int {
        return accountType.id
    }
}

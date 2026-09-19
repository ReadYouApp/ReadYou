package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.hideDuplicateArticles
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalHideDuplicateArticles =
    compositionLocalOf<HideDuplicateArticlesPreference> { HideDuplicateArticlesPreference.default }

sealed class HideDuplicateArticlesPreference(val value: Boolean) : Preference() {
    data object ON : HideDuplicateArticlesPreference(true)
    data object OFF : HideDuplicateArticlesPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                hideDuplicateArticles,
                value
            )
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) = scope.launch {
        context.dataStore.put(
            hideDuplicateArticles,
            !value
        )
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[hideDuplicateArticles]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

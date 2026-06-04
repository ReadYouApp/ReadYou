package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.hideEmptyGroups
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalHideEmptyGroups =
    compositionLocalOf<HideEmptyGroupsPreference> { HideEmptyGroupsPreference.default }

sealed class HideEmptyGroupsPreference(val value: Boolean) : Preference() {
    data object ON : HideEmptyGroupsPreference(true)
    data object OFF : HideEmptyGroupsPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                hideEmptyGroups,
                value
            )
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) = scope.launch {
        context.dataStore.put(
            hideEmptyGroups,
            !value
        )
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[hideEmptyGroups]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

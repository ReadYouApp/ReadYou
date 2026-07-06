package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowSingleColumn
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowSingleColumn =
    compositionLocalOf<FlowSingleColumnPreference> { FlowSingleColumnPreference.default }

sealed class FlowSingleColumnPreference(val value: Boolean) : Preference() {
    object ON : FlowSingleColumnPreference(true)
    object OFF : FlowSingleColumnPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowSingleColumn,
                value
            )
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowSingleColumn]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowSingleColumnPreference.not(): FlowSingleColumnPreference =
    when (value) {
        true -> FlowSingleColumnPreference.OFF
        false -> FlowSingleColumnPreference.ON
    }

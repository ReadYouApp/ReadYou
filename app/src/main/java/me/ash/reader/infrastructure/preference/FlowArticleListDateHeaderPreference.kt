package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListDateHeader
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put


val LocalFlowArticleListDateHeader =
    compositionLocalOf<FlowArticleListDateHeaderPreference>{
        FlowArticleListDateHeaderPreference.default
    }

sealed  class FlowArticleListDateHeaderPreference(val value : Boolean) : Preference() {

    object ON : FlowArticleListDateHeaderPreference(true)
    object OFF  : FlowArticleListDateHeaderPreference(false)
        override fun put(
            context: Context,
            scope: CoroutineScope
        ) {
            scope.launch {
                context.dataStore.put(
                    DataStoreKey.flowArticleListDateHeader,
                    value
                )
            }
        }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListDateHeader]?.key as
                    Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListDateHeaderPreference.not(): FlowArticleListDateHeaderPreference =
    when (value) {
        true -> FlowArticleListDateHeaderPreference.OFF
        false -> FlowArticleListDateHeaderPreference.ON
    }

package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.translateTitle
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalTranslateTitle =
    compositionLocalOf<TranslateTitlePreference> { TranslateTitlePreference.default }

sealed class TranslateTitlePreference(val value: Boolean) : Preference() {
    object ON : TranslateTitlePreference(true)
    object OFF : TranslateTitlePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(translateTitle, value)
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[translateTitle]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun TranslateTitlePreference.not(): TranslateTitlePreference =
    when (value) {
        true -> TranslateTitlePreference.OFF
        false -> TranslateTitlePreference.ON
    }

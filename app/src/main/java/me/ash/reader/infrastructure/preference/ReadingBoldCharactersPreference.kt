package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingBoldCharacters
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingBoldCharacters =
    compositionLocalOf<ReadingBoldCharactersPreference> { ReadingBoldCharactersPreference.default }

sealed class ReadingBoldCharactersPreference(val value: Boolean) : Preference() {
    object ON : ReadingBoldCharactersPreference(true)
    object OFF : ReadingBoldCharactersPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(readingBoldCharacters, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingBoldCharacters]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun ReadingBoldCharactersPreference.not(): ReadingBoldCharactersPreference =
    when (value) {
        true -> ReadingBoldCharactersPreference.OFF
        false -> ReadingBoldCharactersPreference.ON
    }

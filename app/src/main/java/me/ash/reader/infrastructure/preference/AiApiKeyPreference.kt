package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.aiApiKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAiApiKey = compositionLocalOf { AiApiKeyPreference.default }

data class AiApiKeyPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiApiKey, value)
        }
    }

    fun toDesc(context: Context): String = if (value.isNotEmpty()) "••••••••••••" else ""

    companion object {
        val default = AiApiKeyPreference("")

        fun fromPreferences(preferences: Preferences): AiApiKeyPreference {
            return AiApiKeyPreference(
                preferences[DataStoreKey.keys[aiApiKey]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}

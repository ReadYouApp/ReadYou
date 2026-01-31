package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.aiModel
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAiModel = compositionLocalOf { AiModelPreference.default }

data class AiModelPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiModel, value)
        }
    }

    fun toDesc(context: Context): String = value.ifEmpty { context.getString(R.string.ai_model_default) }

    companion object {
        val default = AiModelPreference("")

        fun fromPreferences(preferences: Preferences): AiModelPreference {
            return AiModelPreference(
                preferences[DataStoreKey.keys[aiModel]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}

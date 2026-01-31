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
import me.ash.reader.ui.ext.DataStoreKey.Companion.aiSummarizationPrompt
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalAiSummarizationPrompt = compositionLocalOf { AiSummarizationPromptPreference.default }

data class AiSummarizationPromptPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiSummarizationPrompt, value)
        }
    }

    fun toDesc(context: Context): String = 
        value.ifEmpty { context.getString(R.string.ai_summarization_prompt_default).lines().first() }

    companion object {
        val default = AiSummarizationPromptPreference("")

        fun fromPreferences(preferences: Preferences): AiSummarizationPromptPreference {
            return AiSummarizationPromptPreference(
                preferences[DataStoreKey.keys[aiSummarizationPrompt]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}

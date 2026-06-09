package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.dataStore

typealias KeywordFilters = List<String>

val LocalKeywordFilters = compositionLocalOf<KeywordFilters> { emptyList() }

object KeywordFiltersPreference {

    val default: KeywordFilters = emptyList()

    val preferencesKey: Preferences.Key<String> = stringPreferencesKey("keywordFilters")

    fun put(context: Context, scope: CoroutineScope, keywords: KeywordFilters) {
        scope.launch { context.dataStore.edit { it[preferencesKey] = serialize(keywords) } }
    }

    fun fromPreferences(preferences: Preferences): KeywordFilters =
        deserialize(preferences[preferencesKey] ?: "")

    private fun serialize(keywords: KeywordFilters): String =
        keywords.filter { it.isNotBlank() }.joinToString("\n")

    private fun deserialize(raw: String): KeywordFilters =
        if (raw.isBlank()) emptyList() else raw.split("\n").filter { it.isNotBlank() }
}

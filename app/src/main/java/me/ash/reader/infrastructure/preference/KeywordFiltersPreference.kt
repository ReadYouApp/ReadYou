package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.keywordFilters
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

typealias KeywordFilters = List<String>

val LocalKeywordFilters = compositionLocalOf<KeywordFilters> { emptyList() }

object KeywordFiltersPreference {

    val default: KeywordFilters = emptyList()

    fun put(context: Context, scope: CoroutineScope, filters: KeywordFilters) {
        scope.launch {
            context.dataStore.put(keywordFilters, toStorageString(filters))
        }
    }

    fun fromPreferences(preferences: Preferences): KeywordFilters {
        val raw = preferences[DataStoreKey.keys[keywordFilters]?.key as? Preferences.Key<String>]
            ?: return default
        return of(raw)
    }

    fun of(raw: String): KeywordFilters =
        raw.split("\n").filter { it.isNotBlank() }.map { it.trim() }

    fun toStorageString(filters: KeywordFilters): String =
        filters.filter { it.isNotBlank() }.map { it.trim() }.joinToString("\n")
}

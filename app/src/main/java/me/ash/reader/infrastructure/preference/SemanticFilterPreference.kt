package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.dataStore

val LocalSemanticFilter = compositionLocalOf { false }

object SemanticFilterPreference {

    val default = false

    val preferencesKey: Preferences.Key<Boolean> = booleanPreferencesKey("semanticFilter")

    fun put(context: Context, scope: CoroutineScope, value: Boolean) {
        scope.launch { context.dataStore.edit { it[preferencesKey] = value } }
    }

    fun fromPreferences(preferences: Preferences): Boolean =
        preferences[preferencesKey] ?: default
}

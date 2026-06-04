package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.semanticFilter
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalSemanticFilter = compositionLocalOf { SemanticFilterPreference.default }

class SemanticFilterPreference(val value: Boolean) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(semanticFilter, value)
        }
    }

    fun toggle(context: Context, scope: CoroutineScope) =
        SemanticFilterPreference(!value).put(context, scope)

    companion object {
        val default = SemanticFilterPreference(false)

        fun fromPreferences(preferences: Preferences): SemanticFilterPreference =
            SemanticFilterPreference(
                preferences[DataStoreKey.keys[semanticFilter]?.key as? Preferences.Key<Boolean>]
                    ?: return default
            )
    }
}

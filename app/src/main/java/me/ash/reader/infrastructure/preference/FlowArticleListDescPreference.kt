package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListDesc
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowArticleListDesc =
    compositionLocalOf<FlowArticleListDescPreference> { FlowArticleListDescPreference.default }

sealed class FlowArticleListDescPreference(val value: Int) : Preference() {
    object NONE : FlowArticleListDescPreference(0)
    object SHORT : FlowArticleListDescPreference(1)
    object LONG : FlowArticleListDescPreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowArticleListDesc,
                value
            )
        }
    }

    @Composable
    fun description(): String {
        return when (this) {
            LONG -> stringResource(R.string.long_desc)
            NONE -> stringResource(R.string.none_desc)
            SHORT -> stringResource(R.string.short_desc)
        }
    }

    companion object {

        val default = SHORT
        val values = listOf(NONE, SHORT, LONG)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListDesc]?.key as Preferences.Key<*>]) {
                0 -> NONE
                1 -> SHORT
                2 -> LONG
                else -> default
            }
    }
}

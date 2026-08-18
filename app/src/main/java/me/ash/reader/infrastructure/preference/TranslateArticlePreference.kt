package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.translateArticle
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalTranslateArticle =
    compositionLocalOf<TranslateArticlePreference> { TranslateArticlePreference.default }

sealed class TranslateArticlePreference(val value: Boolean) : Preference() {
    object ON : TranslateArticlePreference(true)
    object OFF : TranslateArticlePreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(translateArticle, value)
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[translateArticle]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun TranslateArticlePreference.not(): TranslateArticlePreference =
    when (value) {
        true -> TranslateArticlePreference.OFF
        false -> TranslateArticlePreference.ON
    }

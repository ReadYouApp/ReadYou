package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.openLink
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

@Deprecated("Use LocalUriHandler instead")
val LocalOpenLink =
    compositionLocalOf<OpenLinkPreference> { OpenLinkPreference.default }

sealed class OpenLinkPreference(val value: Int) : Preference() {
    object AutoPreferCustomTabs : OpenLinkPreference(0)
    object AutoPreferDefaultBrowser : OpenLinkPreference(1)
    object CustomTabs : OpenLinkPreference(2)
    object DefaultBrowser : OpenLinkPreference(3)
    object SpecificBrowser: OpenLinkPreference(4)
    object AlwaysAsk: OpenLinkPreference(5)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.openLink,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            AutoPreferCustomTabs -> context.getString(R.string.auto_customtabs)
            AutoPreferDefaultBrowser -> context.getString(R.string.auto_default_browser)
            CustomTabs -> context.getString(R.string.custom_tabs)
            DefaultBrowser -> context.getString(R.string.default_browser)
            SpecificBrowser -> context.getString(R.string.specific_browser)
            AlwaysAsk -> context.getString(R.string.always_ask)
        }

    companion object {

        val default = CustomTabs
        val values = listOf(AutoPreferCustomTabs, AutoPreferDefaultBrowser, CustomTabs, DefaultBrowser, SpecificBrowser, AlwaysAsk)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[openLink]?.key as Preferences.Key<Int>]) {
                0 -> AutoPreferCustomTabs
                1 -> AutoPreferDefaultBrowser
                2 -> CustomTabs
                3 -> DefaultBrowser
                4 -> SpecificBrowser
                5 -> AlwaysAsk
                else -> AutoPreferCustomTabs
            }
    }
}

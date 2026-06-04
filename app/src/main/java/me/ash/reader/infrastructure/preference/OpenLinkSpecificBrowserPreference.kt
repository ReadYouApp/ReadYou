package me.ash.reader.infrastructure.preference

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.openLinkAppSpecificBrowser
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

@Deprecated("Use LocalUriHandler instead")
val LocalOpenLinkSpecificBrowser = compositionLocalOf { OpenLinkSpecificBrowserPreference.default }

data class OpenLinkSpecificBrowserPreference(
    val packageName: String?
    ) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.openLinkAppSpecificBrowser,
                packageName.orEmpty()
            )
        }
    }

    fun toDesc(context: Context): String {
        val pm = context.packageManager
        return runCatching {
            pm.run {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getApplicationInfo(
                        this@OpenLinkSpecificBrowserPreference.packageName!!,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    getApplicationInfo(this@OpenLinkSpecificBrowserPreference.packageName!!, 0)
                }
            }
        }.map {
            it.loadLabel(pm)
        }.getOrDefault(context.getString(R.string.open_link_specific_browser_not_selected)).let {
            context.getString(R.string.specific_browser_name, it)
        }
    }

    companion object {
        val default = OpenLinkSpecificBrowserPreference(null)
        fun fromPreferences(preferences: Preferences): OpenLinkSpecificBrowserPreference {
            val packageName = preferences[DataStoreKey.keys[openLinkAppSpecificBrowser]?.key as Preferences.Key<String>]
            return OpenLinkSpecificBrowserPreference(packageName)
        }
    }
}

package me.ash.reader.infrastructure.compose

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalSettings
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.infrastructure.preference.OpenLinkSpecificBrowserPreference
import me.ash.reader.ui.ext.getCustomTabsPackages
import me.ash.reader.ui.ext.getDefaultBrowserInfo
import me.ash.reader.ui.ext.showToast

@Composable
internal fun ProvideUriHandler(content: @Composable () -> Unit) {
    val settings = LocalSettings.current
    val context = LocalContext.current
    CompositionLocalProvider(
        LocalUriHandler provides
            AppUriHandler(context, settings.openLink, settings.openLinkSpecificBrowser),
        content = content,
    )
}

internal class AppUriHandler(
    private val context: Context,
    private val openLink: OpenLinkPreference,
    private val specificBrowser: OpenLinkSpecificBrowserPreference,
) : UriHandler {

    override fun openUri(uri: String) {
        val url = uri
        with(context) {
            if (url.isNotBlank()) {
                val uri = url.trim { it.isWhitespace() || it == '\n' }.toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val customTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
                try {
                    when (openLink) {
                        OpenLinkPreference.AlwaysAsk -> {
                            val intents =
                                packageManager
                                    .run {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            queryIntentActivities(
                                                intent,
                                                PackageManager.ResolveInfoFlags.of(
                                                    PackageManager.MATCH_ALL.toLong()
                                                ),
                                            )
                                        } else {
                                            queryIntentActivities(intent, PackageManager.MATCH_ALL)
                                        }
                                    }
                                    .map { Intent(intent).setPackage(it.activityInfo.packageName) }
                                    .toMutableList()
                            val chooser =
                                Intent.createChooser(Intent(), null)
                                    .putExtra(
                                        Intent.EXTRA_ALTERNATE_INTENTS,
                                        intents.toTypedArray<Parcelable>(),
                                    )

                            context.startActivity(chooser)
                        }

                        OpenLinkPreference.AutoPreferCustomTabs -> {
                            customTabsIntent.launchUrl(this, uri)
                        }

                        OpenLinkPreference.AutoPreferDefaultBrowser -> startActivity(intent)
                        OpenLinkPreference.CustomTabs -> {
                            val customTabsPackages = getCustomTabsPackages()
                            require(customTabsPackages.isNotEmpty())
                            val defaultBrowser = getDefaultBrowserInfo()!!.activityInfo.packageName

                            val targetApp =
                                if (customTabsPackages.contains(defaultBrowser)) {
                                    defaultBrowser
                                } else {
                                    customTabsPackages[0]
                                }
                            customTabsIntent.intent.setPackage(targetApp)
                            customTabsIntent.launchUrl(this, uri)
                        }

                        OpenLinkPreference.DefaultBrowser -> {
                            val packageName = getDefaultBrowserInfo()!!.activityInfo.packageName
                            startActivity(intent.setPackage(packageName))
                        }

                        OpenLinkPreference.SpecificBrowser -> {
                            require(!specificBrowser.packageName.isNullOrBlank())
                            startActivity(intent.setPackage(specificBrowser.packageName))
                        }
                    }
                } catch (_: Throwable) {
                    showToast(getString(R.string.open_link_something_wrong))
                    startActivity(intent)
                }
            }
        }
    }
}

package me.ash.reader.ui.page.nav3.key

import androidx.compose.runtime.saveable.Saver
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    // Startup
    @Serializable data object Startup : Route

    // Home
    @Serializable data object Feeds : Route

    //    @Serializable data object Flow : Route

    @Serializable
    data class Reading(val articleId: String?) : Route {
        companion object {
            val Saver = Saver<Reading, String>(save = { it.articleId }, restore = { Reading(it) })
        }
    }

    // Settings
    @Serializable data object Settings : Route

    // Accounts
    @Serializable data object Accounts : Route

    @Serializable data class AccountDetails(val accountId: Int) : Route

    @Serializable data object AddAccounts : Route

    // Color & Style
    @Serializable data object ColorAndStyle : Route

    @Serializable data object DarkTheme : Route

    @Serializable data object FeedsPageStyle : Route

    @Serializable data object FlowPageStyle : Route

    @Serializable data object ReadingPageStyle : Route

    @Serializable data object ReadingBoldCharacters : Route

    @Serializable data object ReadingPageTitle : Route

    @Serializable data object ReadingPageText : Route

    @Serializable data object ReadingPageImage : Route

    @Serializable data object ReadingPageVideo : Route

    // Interaction
    @Serializable data object Interaction : Route

    // AI
    @Serializable data object AiSettings : Route

    // Languages
    @Serializable data object Languages : Route

    // Troubleshooting
    @Serializable data object Troubleshooting : Route

    // Tips & Support
    @Serializable data object TipsAndSupport : Route

    @Serializable data object LicenseList : Route
}

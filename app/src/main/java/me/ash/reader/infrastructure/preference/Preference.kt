package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.PreferenceKey
import org.json.JSONObject

sealed interface AppPreference {
    val value: Any
    val key: PreferenceKey

    interface IntPreference : AppPreference {
        override val value: Int
        override val key: PreferenceKey.IntKey
    }

    interface LongPreference : AppPreference {
        override val value: Long
        override val key: PreferenceKey.LongKey
    }

    interface StringPreference : AppPreference {
        override val value: String
        override val key: PreferenceKey.StringKey
    }

    interface BooleanPreference : AppPreference {
        override val value: Boolean
        override val key: PreferenceKey.BooleanKey
    }

    interface FloatPreference : AppPreference {
        override val value: Float
        override val key: PreferenceKey.FloatKey
    }

    interface PreferenceCompanion {
        val key: PreferenceKey
        val default: AppPreference

        fun fromPreferences(preferences: Preferences): AppPreference

        val values: List<AppPreference>
    }

    sealed interface Editable {
        suspend fun put(context: Context)

        @Deprecated("Use the suspend function instead")
        fun put(context: Context, coroutineScope: CoroutineScope) =
            coroutineScope.launch { put(context) }
    }
}

fun JSONObject.put(preference: AppPreference) {
    when (preference) {
        is AppPreference.BooleanPreference -> put(preference.key.name, preference.value)
        is AppPreference.FloatPreference -> put(preference.key.name, preference.value)
        is AppPreference.IntPreference -> put(preference.key.name, preference.value)
        is AppPreference.LongPreference -> put(preference.key.name, preference.value)
        is AppPreference.StringPreference -> put(preference.key.name, preference.value)
    }
}

sealed class Preference {

    abstract fun put(context: Context, scope: CoroutineScope)
}

fun Preferences.toSettings(): Settings {
    return Settings(
        // Version
        newVersionNumber = NewVersionNumberPreference.fromPreferences(this),
        skipVersionNumber = SkipVersionNumberPreference.fromPreferences(this),
        newVersionPublishDate = NewVersionPublishDatePreference.fromPreferences(this),
        newVersionLog = NewVersionLogPreference.fromPreferences(this),
        newVersionSize = NewVersionSizePreference.fromPreferences(this),
        newVersionDownloadUrl = NewVersionDownloadUrlPreference.fromPreferences(this),

        // Theme
        themeIndex = ThemeIndexPreference.fromPreferences(this),
        customPrimaryColor = CustomPrimaryColorPreference.fromPreferences(this),
        darkTheme = DarkThemePreference.fromPreferences(this),
        amoledDarkTheme = AmoledDarkThemePreference.fromPreferences(this),
        basicFonts = BasicFontsPreference.fromPreferences(this),

        // Feeds page
        feedsFilterBarStyle = FeedsFilterBarStylePreference.fromPreferences(this),
        feedsFilterBarPadding = FeedsFilterBarPaddingPreference.fromPreferences(this),
        feedsFilterBarTonalElevation = FeedsFilterBarTonalElevationPreference.fromPreferences(this),
        feedsTopBarTonalElevation = FeedsTopBarTonalElevationPreference.fromPreferences(this),
        feedsGroupListExpand = FeedsGroupListExpandPreference.fromPreferences(this),
        feedsGroupListTonalElevation = FeedsGroupListTonalElevationPreference.fromPreferences(this),

        // Flow page
        flowFilterBarStyle = FlowFilterBarStylePreference.fromPreferences(this),
        flowFilterBarPadding = FlowFilterBarPaddingPreference.fromPreferences(this),
        flowFilterBarTonalElevation = FlowFilterBarTonalElevationPreference.fromPreferences(this),
        flowTopBarTonalElevation = FlowTopBarTonalElevationPreference.fromPreferences(this),
        flowArticleListFeedIcon = FlowArticleListFeedIconPreference.fromPreferences(this),
        flowArticleListFeedName = FlowArticleListFeedNamePreference.fromPreferences(this),
        flowArticleListImage = FlowArticleListImagePreference.fromPreferences(this),
        flowArticleListDesc = FlowArticleListDescPreference.fromPreferences(this),
        flowArticleListTime = FlowArticleListTimePreference.fromPreferences(this),
        flowArticleListDateStickyHeader =
            FlowArticleListDateStickyHeaderPreference.fromPreferences(this),
        flowArticleListReadIndicator = FlowArticleReadIndicatorPreference.fromPreferences(this),
        flowArticleListTonalElevation =
            FlowArticleListTonalElevationPreference.fromPreferences(this),
        flowSortUnreadArticles = SortUnreadArticlesPreference.fromPreferences(this),

        // Reading page
        readingRenderer = ReadingRendererPreference.fromPreferences(this),
        readingBoldCharacters = ReadingBoldCharactersPreference.fromPreferences(this),
        readingTheme = ReadingThemePreference.fromPreferences(this),
        readingPageTonalElevation = ReadingPageTonalElevationPreference.fromPreferences(this),
        readingAutoHideToolbar = ReadingAutoHideToolbarPreference.fromPreferences(this),
        readingTextFontSize = ReadingTextFontSizePreference.fromPreferences(this),
        readingTextLineHeight = ReadingTextLineHeightPreference.fromPreferences(this),
        readingLetterSpacing = ReadingTextLetterSpacingPreference.fromPreferences(this),
        readingTextHorizontalPadding = ReadingTextHorizontalPaddingPreference.fromPreferences(this),
        readingTextAlign = ReadingTextAlignPreference.fromPreferences(this),
        readingTextBold = ReadingTextBoldPreference.fromPreferences(this),
        readingTitleAlign = ReadingTitleAlignPreference.fromPreferences(this),
        readingSubheadAlign = ReadingSubheadAlignPreference.fromPreferences(this),
        readingFonts = ReadingFontsPreference.fromPreferences(this),
        readingTitleBold = ReadingTitleBoldPreference.fromPreferences(this),
        readingSubheadBold = ReadingSubheadBoldPreference.fromPreferences(this),
        readingTitleUpperCase = ReadingTitleUpperCasePreference.fromPreferences(this),
        readingSubheadUpperCase = ReadingSubheadUpperCasePreference.fromPreferences(this),
        readingImageHorizontalPadding =
            ReadingImageHorizontalPaddingPreference.fromPreferences(this),
        readingImageRoundedCorners = ReadingImageRoundedCornersPreference.fromPreferences(this),
        readingImageMaximize = ReadingImageMaximizePreference.fromPreferences(this),

        // Interaction
        initialPage = InitialPagePreference.fromPreferences(this),
        initialFilter = InitialFilterPreference.fromPreferences(this),
        swipeStartAction = SwipeStartActionPreference.fromPreferences(this),
        swipeEndAction = SwipeEndActionPreference.fromPreferences(this),
        markAsReadOnScroll = MarkAsReadOnScrollPreference.fromPreferences(this),
        hideEmptyGroups = HideEmptyGroupsPreference.fromPreferences(this),
        pullToSwitchFeed = PullToLoadNextFeedPreference.fromPreference(this),
        pullToSwitchArticle = PullToSwitchArticlePreference.fromPreference(this),
        openLink = OpenLinkPreference.fromPreferences(this),
        openLinkSpecificBrowser = OpenLinkSpecificBrowserPreference.fromPreferences(this),
        sharedContent = SharedContentPreference.fromPreferences(this),

        // Languages
        languages = LanguagesPreference.fromPreferences(this),
    )
}

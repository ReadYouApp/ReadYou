package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.ash.reader.infrastructure.datastore.get
import me.ash.reader.infrastructure.datastore.getOrDefault
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.dataStore
import javax.inject.Inject

class SettingsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope coroutineScope: CoroutineScope,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) {
    private val _settingsFlow = MutableStateFlow(Settings())
    val settingsFlow: StateFlow<Settings> = _settingsFlow
    val settings: Settings get() = settingsFlow.value

    val dataStore = context.dataStore.data

    val preferencesFlow: StateFlow<Preferences> =
        dataStore.stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = preferencesOf()
        )

    val preferences get() = preferencesFlow.value

    inline fun <reified T> get(key: Preferences.Key<T>): T? = preferences[key]

    inline fun <reified T> get(key: String): T? = preferences.get(key)

    inline fun <reified T> getOrDefault(key: String, default: T): T =
        preferences.getOrDefault(key, default) ?: default


    init {
        coroutineScope.launch(ioDispatcher) {
            preferencesFlow.collect {
                _settingsFlow.value = it.toSettings()
            }
        }
    }

    @Composable
    fun ProvidesSettings(content: @Composable () -> Unit) {
        val settings = settingsFlow.collectAsStateValue()
        CompositionLocalProvider(
            LocalSettings provides settings,

            // Version
            NewVersionNumberPreference.provide(settings),
            LocalSkipVersionNumber provides settings.skipVersionNumber,
            LocalNewVersionPublishDate provides settings.newVersionPublishDate,
            LocalNewVersionLog provides settings.newVersionLog,
            LocalNewVersionSize provides settings.newVersionSize,
            LocalNewVersionDownloadUrl provides settings.newVersionDownloadUrl,
            LocalBasicFonts provides settings.basicFonts,

            // Theme
            LocalThemeIndex provides settings.themeIndex,
            LocalCustomPrimaryColor provides settings.customPrimaryColor,
            LocalDarkTheme provides settings.darkTheme,
            LocalAmoledDarkTheme provides settings.amoledDarkTheme,
            LocalBasicFonts provides settings.basicFonts,

            // Feeds page
            LocalFeedsTopBarTonalElevation provides settings.feedsTopBarTonalElevation,
            LocalFeedsGroupListExpand provides settings.feedsGroupListExpand,
            LocalFeedsGroupListTonalElevation provides settings.feedsGroupListTonalElevation,
            LocalFeedsFilterBarStyle provides settings.feedsFilterBarStyle,
            LocalFeedsFilterBarPadding provides settings.feedsFilterBarPadding,
            LocalFeedsFilterBarTonalElevation provides settings.feedsFilterBarTonalElevation,

            // Flow page
            LocalFlowTopBarTonalElevation provides settings.flowTopBarTonalElevation,
            LocalFlowArticleListFeedIcon provides settings.flowArticleListFeedIcon,
            LocalFlowArticleListFeedName provides settings.flowArticleListFeedName,
            LocalFlowArticleListImage provides settings.flowArticleListImage,
            LocalFlowArticleListDesc provides settings.flowArticleListDesc,
            LocalFlowArticleListTime provides settings.flowArticleListTime,
            LocalFlowArticleListDateStickyHeader provides settings.flowArticleListDateStickyHeader,
            LocalFlowArticleListTonalElevation provides settings.flowArticleListTonalElevation,
            LocalFlowFilterBarStyle provides settings.flowFilterBarStyle,
            LocalFlowFilterBarPadding provides settings.flowFilterBarPadding,
            LocalFlowFilterBarTonalElevation provides settings.flowFilterBarTonalElevation,
            LocalFlowArticleListReadIndicator provides settings.flowArticleListReadIndicator,
            LocalSortUnreadArticles provides settings.flowSortUnreadArticles,

            // Reading page
            LocalReadingRenderer provides settings.readingRenderer,
            LocalReadingBoldCharacters provides settings.readingBoldCharacters,
            LocalReadingTheme provides settings.readingTheme,
            LocalReadingPageTonalElevation provides settings.readingPageTonalElevation,
            LocalReadingAutoHideToolbar provides settings.readingAutoHideToolbar,
            LocalReadingTextFontSize provides settings.readingTextFontSize,
            LocalReadingTextLineHeight provides settings.readingTextLineHeight,
            LocalReadingTextLetterSpacing provides settings.readingLetterSpacing,
            LocalReadingTextHorizontalPadding provides settings.readingTextHorizontalPadding,
            LocalReadingTextAlign provides settings.readingTextAlign,
            LocalReadingTextBold provides settings.readingTextBold,
            LocalReadingTitleAlign provides settings.readingTitleAlign,
            LocalReadingSubheadAlign provides settings.readingSubheadAlign,
            LocalReadingFonts provides settings.readingFonts,
            LocalReadingTitleBold provides settings.readingTitleBold,
            LocalReadingSubheadBold provides settings.readingSubheadBold,
            LocalReadingTitleUpperCase provides settings.readingTitleUpperCase,
            LocalReadingSubheadUpperCase provides settings.readingSubheadUpperCase,
            LocalReadingImageHorizontalPadding provides settings.readingImageHorizontalPadding,
            LocalReadingImageRoundedCorners provides settings.readingImageRoundedCorners,
            LocalReadingImageMaximize provides settings.readingImageMaximize,

            // Interaction
            LocalInitialPage provides settings.initialPage,
            LocalInitialFilter provides settings.initialFilter,
            LocalArticleListSwipeStartAction provides settings.swipeStartAction,
            LocalArticleListSwipeEndAction provides settings.swipeEndAction,
            LocalMarkAsReadOnScroll provides settings.markAsReadOnScroll,
            LocalHideEmptyGroups provides settings.hideEmptyGroups,
            LocalPullToSwitchArticle provides settings.pullToSwitchArticle,
            LocalOpenLink provides settings.openLink,
            LocalOpenLinkSpecificBrowser provides settings.openLinkSpecificBrowser,
            LocalSharedContent provides settings.sharedContent,

            // Languages
            LocalLanguages provides settings.languages,

            // AI
            LocalAiBaseUrl provides settings.aiBaseUrl,
            LocalAiApiKey provides settings.aiApiKey,
            LocalAiModel provides settings.aiModel,
            LocalAiSummarizationPrompt provides settings.aiSummarizationPrompt,
        ) {
            content()
        }
    }
}
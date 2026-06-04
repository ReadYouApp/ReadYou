package me.ash.reader.infrastructure.preference

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import me.ash.reader.domain.model.general.Version
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.dataStore

data class Settings(
    // Version
    val newVersionNumber: Version = NewVersionNumberPreference.default,
    val skipVersionNumber: Version = SkipVersionNumberPreference.default,
    val newVersionPublishDate: String = NewVersionPublishDatePreference.default,
    val newVersionLog: String = NewVersionLogPreference.default,
    val newVersionSize: String = NewVersionSizePreference.default,
    val newVersionDownloadUrl: String = NewVersionDownloadUrlPreference.default,

    // Theme
    val themeIndex: Int = ThemeIndexPreference.default,
    val customPrimaryColor: String = CustomPrimaryColorPreference.default,
    val darkTheme: DarkThemePreference = DarkThemePreference.default,
    val amoledDarkTheme: AmoledDarkThemePreference = AmoledDarkThemePreference.default,
    val basicFonts: BasicFontsPreference = BasicFontsPreference.default,

    // Feeds page
    val feedsFilterBarStyle: FeedsFilterBarStylePreference = FeedsFilterBarStylePreference.default,
    val feedsFilterBarFilled: FeedsFilterBarFilledPreference = FeedsFilterBarFilledPreference.default,
    val feedsFilterBarPadding: Int = FeedsFilterBarPaddingPreference.default,
    val feedsFilterBarTonalElevation: FeedsFilterBarTonalElevationPreference = FeedsFilterBarTonalElevationPreference.default,
    val feedsTopBarTonalElevation: FeedsTopBarTonalElevationPreference = FeedsTopBarTonalElevationPreference.default,
    val feedsGroupListExpand: FeedsGroupListExpandPreference = FeedsGroupListExpandPreference.default,
    val feedsGroupListTonalElevation: FeedsGroupListTonalElevationPreference = FeedsGroupListTonalElevationPreference.default,

    // Flow page
    val flowFilterBarStyle: FlowFilterBarStylePreference = FlowFilterBarStylePreference.default,
    val flowFilterBarFilled: FlowFilterBarFilledPreference = FlowFilterBarFilledPreference.default,
    val flowFilterBarPadding: Int = FlowFilterBarPaddingPreference.default,
    val flowFilterBarTonalElevation: FlowFilterBarTonalElevationPreference = FlowFilterBarTonalElevationPreference.default,
    val flowTopBarTonalElevation: FlowTopBarTonalElevationPreference = FlowTopBarTonalElevationPreference.default,
    val flowArticleListFeedIcon: FlowArticleListFeedIconPreference = FlowArticleListFeedIconPreference.default,
    val flowArticleListFeedName: FlowArticleListFeedNamePreference = FlowArticleListFeedNamePreference.default,
    val flowArticleListImage: FlowArticleListImagePreference = FlowArticleListImagePreference.default,
    val flowArticleListDesc: FlowArticleListDescPreference = FlowArticleListDescPreference.default,
    val flowArticleListTime: FlowArticleListTimePreference = FlowArticleListTimePreference.default,
    val flowArticleListDateStickyHeader: FlowArticleListDateStickyHeaderPreference = FlowArticleListDateStickyHeaderPreference.default,
    val flowArticleListTonalElevation: FlowArticleListTonalElevationPreference = FlowArticleListTonalElevationPreference.default,
    val flowArticleListReadIndicator: FlowArticleReadIndicatorPreference = FlowArticleReadIndicatorPreference.default,

    // Reading page
    val readingRenderer: ReadingRendererPreference = ReadingRendererPreference.default,
    val readingBionicReading: ReadingBionicReadingPreference = ReadingBionicReadingPreference.default,
    val readingTheme: ReadingThemePreference = ReadingThemePreference.default,
    val readingDarkTheme: ReadingDarkThemePreference = ReadingDarkThemePreference.default,
    val readingPageTonalElevation: ReadingPageTonalElevationPreference = ReadingPageTonalElevationPreference.default,
    val readingAutoHideToolbar: ReadingAutoHideToolbarPreference = ReadingAutoHideToolbarPreference.default,
    val readingTextFontSize: Int = ReadingTextFontSizePreference.default,
    val readingTextLineHeight: Float = ReadingTextLineHeightPreference.default,
    val readingLetterSpacing: Float = ReadingTextLetterSpacingPreference.default,
    val readingTextHorizontalPadding: Int = ReadingTextHorizontalPaddingPreference.default,
    val readingTextAlign: ReadingTextAlignPreference = ReadingTextAlignPreference.default,
    val readingTextBold: ReadingTextBoldPreference = ReadingTextBoldPreference.default,
    val readingTitleAlign: ReadingTitleAlignPreference = ReadingTitleAlignPreference.default,
    val readingSubheadAlign: ReadingSubheadAlignPreference = ReadingSubheadAlignPreference.default,
    val readingFonts: ReadingFontsPreference = ReadingFontsPreference.default,
    val readingTitleBold: ReadingTitleBoldPreference = ReadingTitleBoldPreference.default,
    val readingSubheadBold: ReadingSubheadBoldPreference = ReadingSubheadBoldPreference.default,
    val readingTitleUpperCase: ReadingTitleUpperCasePreference = ReadingTitleUpperCasePreference.default,
    val readingSubheadUpperCase: ReadingSubheadUpperCasePreference = ReadingSubheadUpperCasePreference.default,
    val readingImageHorizontalPadding: Int = ReadingImageHorizontalPaddingPreference.default,
    val readingImageRoundedCorners: Int = ReadingImageRoundedCornersPreference.default,
    val readingImageMaximize: ReadingImageMaximizePreference = ReadingImageMaximizePreference.default,

    // Interaction
    val initialPage: InitialPagePreference = InitialPagePreference.default,
    val initialFilter: InitialFilterPreference = InitialFilterPreference.default,
    val swipeStartAction: SwipeStartActionPreference = SwipeStartActionPreference.default,
    val swipeEndAction: SwipeEndActionPreference = SwipeEndActionPreference.default,
    val pullToSwitchArticle: PullToSwitchArticlePreference = PullToSwitchArticlePreference.default,
    val openLink: OpenLinkPreference = OpenLinkPreference.default,
    val openLinkSpecificBrowser: OpenLinkSpecificBrowserPreference = OpenLinkSpecificBrowserPreference.default,
    val sharedContent: SharedContentPreference = SharedContentPreference.default,

    // Languages
    val languages: LanguagesPreference = LanguagesPreference.default,

    // Filters
    val keywordFilters: KeywordFilters = KeywordFiltersPreference.default,
    val semanticFilter: SemanticFilterPreference = SemanticFilterPreference.default,
)

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember {
        context.dataStore.data.map {
            Log.i("RLog", "AppTheme: ${it}")
            it.toSettings()
        }
    }.collectAsStateValue(initial = Settings())

    CompositionLocalProvider(
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
        LocalFeedsFilterBarFilled provides settings.feedsFilterBarFilled,
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
        LocalFlowFilterBarFilled provides settings.flowFilterBarFilled,
        LocalFlowFilterBarPadding provides settings.flowFilterBarPadding,
        LocalFlowFilterBarTonalElevation provides settings.flowFilterBarTonalElevation,
        LocalFlowArticleListReadIndicator provides settings.flowArticleListReadIndicator,

        // Reading page
        LocalReadingRenderer provides settings.readingRenderer,
        LocalReadingBionicReading provides settings.readingBionicReading,
        LocalReadingTheme provides settings.readingTheme,
        LocalReadingDarkTheme provides settings.readingDarkTheme,
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
        LocalPullToSwitchArticle provides settings.pullToSwitchArticle,
        LocalOpenLink provides settings.openLink,
        LocalOpenLinkSpecificBrowser provides settings.openLinkSpecificBrowser,
        LocalSharedContent provides settings.sharedContent,

        // Languages
        LocalLanguages provides settings.languages,

        // Filters
        LocalKeywordFilters provides settings.keywordFilters,
        LocalSemanticFilter provides settings.semanticFilter,
    ) {
        content()
    }
}


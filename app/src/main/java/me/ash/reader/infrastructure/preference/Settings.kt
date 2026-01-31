package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import me.ash.reader.domain.model.general.Version

val LocalSettings = compositionLocalOf { Settings() }

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
    val feedsFilterBarPadding: Int = FeedsFilterBarPaddingPreference.default,
    val feedsFilterBarTonalElevation: FeedsFilterBarTonalElevationPreference = FeedsFilterBarTonalElevationPreference.default,
    val feedsTopBarTonalElevation: FeedsTopBarTonalElevationPreference = FeedsTopBarTonalElevationPreference.default,
    val feedsGroupListExpand: FeedsGroupListExpandPreference = FeedsGroupListExpandPreference.default,
    val feedsGroupListTonalElevation: FeedsGroupListTonalElevationPreference = FeedsGroupListTonalElevationPreference.default,

    // Flow page
    val flowFilterBarStyle: FlowFilterBarStylePreference = FlowFilterBarStylePreference.default,
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
    val flowSortUnreadArticles: SortUnreadArticlesPreference = SortUnreadArticlesPreference.default,

    // Reading page
    val readingRenderer: ReadingRendererPreference = ReadingRendererPreference.default,
    val readingBoldCharacters: ReadingBoldCharactersPreference = ReadingBoldCharactersPreference.default,
    val readingTheme: ReadingThemePreference = ReadingThemePreference.default,
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
    val markAsReadOnScroll: MarkAsReadOnScrollPreference = MarkAsReadOnScrollPreference.default,
    val hideEmptyGroups: HideEmptyGroupsPreference = HideEmptyGroupsPreference.default,
    val pullToSwitchFeed: PullToLoadNextFeedPreference = PullToLoadNextFeedPreference.default,
    val pullToSwitchArticle: PullToSwitchArticlePreference = PullToSwitchArticlePreference.default,
    val openLink: OpenLinkPreference = OpenLinkPreference.default,
    val openLinkSpecificBrowser: OpenLinkSpecificBrowserPreference = OpenLinkSpecificBrowserPreference.default,
    val sharedContent: SharedContentPreference = SharedContentPreference.default,

    // Languages
    val languages: LanguagesPreference = LanguagesPreference.default,

    // AI
    val aiBaseUrl: AiBaseUrlPreference = AiBaseUrlPreference.default,
    val aiApiKey: AiApiKeyPreference = AiApiKeyPreference.default,
    val aiModel: AiModelPreference = AiModelPreference.default,
    val aiSummarizationPrompt: AiSummarizationPromptPreference = AiSummarizationPromptPreference.default,
)


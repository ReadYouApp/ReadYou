package me.ash.reader.ui.ext

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.ash.reader.ui.ext.PreferenceKey.BooleanKey
import me.ash.reader.ui.ext.PreferenceKey.FloatKey
import me.ash.reader.ui.ext.PreferenceKey.IntKey
import me.ash.reader.ui.ext.PreferenceKey.LongKey
import me.ash.reader.ui.ext.PreferenceKey.StringKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.skipVersionNumber: String
    get() = this.dataStore.get(DataStoreKey.skipVersionNumber) ?: ""
val Context.isFirstLaunch: Boolean
    get() = this.dataStore.get(DataStoreKey.isFirstLaunch) ?: true

@Deprecated("Use AccountService to retrieve the current account")
val Context.currentAccountId: Int
    get() = this.dataStore.get(DataStoreKey.currentAccountId) ?: 1
@Deprecated("Use AccountService to retrieve the current account")
val Context.currentAccountType: Int
    get() = this.dataStore.get(DataStoreKey.currentAccountType) ?: 1

val Context.initialPage: Int
    get() = this.dataStore.get(DataStoreKey.initialPage) ?: 0
val Context.initialFilter: Int
    get() = this.dataStore.get(DataStoreKey.initialFilter) ?: 2

val Context.languages: Int
    get() = this.dataStore.get(DataStoreKey.languages) ?: 0

@Deprecated("Use AppPreference.Editable.put instead")
suspend fun DataStore<Preferences>.put(dataStoreKeys: String, value: Any) {
    val key = PreferenceKey.keys[dataStoreKeys] ?: return
    this.edit {
        withContext(Dispatchers.IO) {
            when (key) {
                is BooleanKey -> if (value is Boolean) it[key] = value
                is FloatKey -> if (value is Float) it[key] = value
                is IntKey -> if (value is Int) it[key] = value
                is LongKey -> if (value is Long) it[key] = value
                is StringKey -> if (value is String) it[key] = value
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> DataStore<Preferences>.get(key: String): T? {
    return runBlocking {
        val key = PreferenceKey.keys[key] ?: return@runBlocking null
        this@get.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()[key.key]
            as? T
    }
}

operator fun Preferences.get(key: IntKey) = this[key.key]

operator fun MutablePreferences.set(key: IntKey, value: Int) = this.set(key.key, value)

operator fun Preferences.get(key: LongKey) = this[key.key]

operator fun MutablePreferences.set(key: LongKey, value: Long) = this.set(key.key, value)

operator fun Preferences.get(key: StringKey) = this[key.key]

operator fun MutablePreferences.set(key: StringKey, value: String) = this.set(key.key, value)

operator fun Preferences.get(key: BooleanKey) = this[key.key]

operator fun MutablePreferences.set(key: BooleanKey, value: Boolean) = this.set(key.key, value)

operator fun Preferences.get(key: FloatKey) = this[key.key]

operator fun MutablePreferences.set(key: FloatKey, value: Float) = this.set(key.key, value)

sealed interface PreferenceKey {
    val key: Preferences.Key<*>
    val name: String
        get() = key.name

    data class IntKey(override val key: Preferences.Key<Int>) : PreferenceKey {
        constructor(name: String) : this(intPreferencesKey(name))
    }

    data class LongKey(override val key: Preferences.Key<Long>) : PreferenceKey {
        constructor(name: String) : this(longPreferencesKey(name))
    }

    data class StringKey(override val key: Preferences.Key<String>) : PreferenceKey {
        constructor(name: String) : this(stringPreferencesKey(name))
    }

    data class BooleanKey(override val key: Preferences.Key<Boolean>) : PreferenceKey {
        constructor(name: String) : this(booleanPreferencesKey(name))
    }

    data class FloatKey(override val key: Preferences.Key<Float>) : PreferenceKey {
        constructor(name: String) : this(floatPreferencesKey(name))
    }

    companion object {
        // Version
        private const val isFirstLaunch = "isFirstLaunch"
        private const val newVersionPublishDate = "newVersionPublishDate"
        private const val newVersionLog = "newVersionLog"
        private const val newVersionSizeString = "newVersionSizeString"
        private const val newVersionDownloadUrl = "newVersionDownloadUrl"
        private const val newVersionNumber = "newVersionNumber"
        private const val skipVersionNumber = "skipVersionNumber"
        private const val currentAccountId = "currentAccountId"
        private const val currentAccountType = "currentAccountType"
        private const val themeIndex = "themeIndex"
        private const val customPrimaryColor = "customPrimaryColor"
        private const val darkTheme = "darkTheme"
        private const val amoledDarkTheme = "amoledDarkTheme"
        private const val basicFonts = "basicFonts"

        // Feeds page
        private const val feedsFilterBarStyle = "feedsFilterBarStyle"
        private const val feedsFilterBarPadding = "feedsFilterBarPadding"
        private const val feedsFilterBarTonalElevation = "feedsFilterBarTonalElevation"
        private const val feedsTopBarTonalElevation = "feedsTopBarTonalElevation"
        private const val feedsGroupListExpand = "feedsGroupListExpand"
        private const val feedsGroupListTonalElevation = "feedsGroupListTonalElevation"

        // Flow page
        private const val flowFilterBarStyle = "flowFilterBarStyle"
        private const val flowFilterBarPadding = "flowFilterBarPadding"
        private const val flowFilterBarTonalElevation = "flowFilterBarTonalElevation"
        private const val flowTopBarTonalElevation = "flowTopBarTonalElevation"
        private const val flowArticleListFeedIcon = "flowArticleListFeedIcon"
        private const val flowArticleListFeedName = "flowArticleListFeedName"
        private const val flowArticleListImage = "flowArticleListImage"
        private const val flowArticleListDesc = "flowArticleListDescription"
        private const val flowArticleListTime = "flowArticleListTime"
        private const val flowArticleListDateStickyHeader = "flowArticleListDateStickyHeader"
        private const val flowArticleListTonalElevation = "flowArticleListTonalElevation"
        private const val flowArticleListReadIndicator = "flowArticleListReadStatusIndicator"
        private const val flowSortUnreadArticles = "flowArticleListSortUnreadArticles"

        // Reading page
        private const val readingRenderer = "readingRender"
        private const val readingBoldCharacters = "readingBoldCharacters"
        private const val readingPageTonalElevation = "readingPageTonalElevation"
        private const val readingTextFontSize = "readingTextFontSize"
        private const val readingTextLineHeight = "readingTextLineHeight"
        private const val readingTextLetterSpacing = "readingTextLetterSpacing"
        private const val readingTextHorizontalPadding = "readingTextHorizontalPadding"
        private const val readingTextBold = "readingTextBold"
        private const val readingTextAlign = "readingTextAlign"
        private const val readingTitleAlign = "readingTitleAlign"
        private const val readingSubheadAlign = "readingSubheadAlign"
        private const val readingTheme = "readingTheme"
        private const val readingFonts = "readingFonts"
        private const val readingAutoHideToolbar = "readingAutoHideToolbar"
        private const val readingTitleBold = "readingTitleBold"
        private const val readingSubheadBold = "readingSubheadBold"
        private const val readingTitleUpperCase = "readingTitleUpperCase"
        private const val readingSubheadUpperCase = "readingSubheadUpperCase"
        private const val readingImageMaximize = "readingImageMaximize"
        private const val readingImageHorizontalPadding = "readingImageHorizontalPadding"
        private const val readingImageRoundedCorners = "readingImageRoundedCorners"

        // Interaction
        private const val initialPage = "initialPage"
        private const val initialFilter = "initialFilter"
        private const val swipeStartAction = "swipeStartAction"
        private const val swipeEndAction = "swipeEndAction"
        private const val markAsReadOnScroll = "markAsReadOnScroll"
        private const val hideEmptyGroups = "hideEmptyGroups"
        private const val pullToLoadNextFeed = "pullToLoadNextFeed"
        private const val pullToSwitchArticle = "pullToSwitchArticle"
        private const val openLink = "openLink"
        private const val openLinkAppSpecificBrowser = "openLinkAppSpecificBrowser"
        private const val sharedContent = "sharedContent"

        // Languages
        private const val languages = "languages"

        val IsFirstLaunch = BooleanKey(isFirstLaunch)
        val NewVersionPublishDate = StringKey(newVersionPublishDate)
        val NewVersionLog = StringKey(newVersionLog)
        val NewVersionSizeString = StringKey(newVersionSizeString)
        val NewVersionDownloadUrl = StringKey(newVersionDownloadUrl)
        val NewVersionNumber = StringKey(newVersionNumber)
        val SkipVersionNumber = StringKey(skipVersionNumber)
        val CurrentAccountId = IntKey(currentAccountId)
        val CurrentAccountType = IntKey(currentAccountType)
        val ThemeIndex = IntKey(themeIndex)
        val CustomPrimaryColor = StringKey(customPrimaryColor)
        val DarkTheme = IntKey(darkTheme)
        val AmoledDarkTheme = BooleanKey(amoledDarkTheme)
        val BasicFonts = IntKey(basicFonts)
        val FeedsFilterBarStyle = IntKey(feedsFilterBarStyle)
        val FeedsFilterBarPadding = IntKey(feedsFilterBarPadding)
        val FeedsFilterBarTonalElevation = IntKey(feedsFilterBarTonalElevation)
        val FeedsTopBarTonalElevation = IntKey(feedsTopBarTonalElevation)
        val FeedsGroupListExpand = BooleanKey(feedsGroupListExpand)
        val FeedsGroupListTonalElevation = IntKey(feedsGroupListTonalElevation)
        val FlowFilterBarStyle = IntKey(flowFilterBarStyle)
        val FlowFilterBarPadding = IntKey(flowFilterBarPadding)
        val FlowFilterBarTonalElevation = IntKey(flowFilterBarTonalElevation)
        val FlowTopBarTonalElevation = IntKey(flowTopBarTonalElevation)
        val FlowArticleListFeedIcon = BooleanKey(flowArticleListFeedIcon)
        val FlowArticleListFeedName = BooleanKey(flowArticleListFeedName)
        val FlowArticleListImage = BooleanKey(flowArticleListImage)
        val FlowArticleListDesc = BooleanKey(flowArticleListDesc)
        val FlowArticleListTime = BooleanKey(flowArticleListTime)
        val FlowArticleListDateStickyHeader = BooleanKey(flowArticleListDateStickyHeader)
        val FlowArticleListTonalElevation = IntKey(flowArticleListTonalElevation)
        val FlowArticleListReadIndicator = IntKey(flowArticleListReadIndicator)
        val FlowSortUnreadArticles = BooleanKey(flowSortUnreadArticles)
        val ReadingRenderer = IntKey(readingRenderer)
        val ReadingBoldCharacters = BooleanKey(readingBoldCharacters)
        val ReadingPageTonalElevation = IntKey(readingPageTonalElevation)
        val ReadingTextFontSize = IntKey(readingTextFontSize)
        val ReadingTextLineHeight = FloatKey(readingTextLineHeight)
        val ReadingTextLetterSpacing = FloatKey(readingTextLetterSpacing)
        val ReadingTextHorizontalPadding = IntKey(readingTextHorizontalPadding)
        val ReadingTextBold = BooleanKey(readingTextBold)
        val ReadingTextAlign = IntKey(readingTextAlign)
        val ReadingTitleAlign = IntKey(readingTitleAlign)
        val ReadingSubheadAlign = IntKey(readingSubheadAlign)
        val ReadingTheme = IntKey(readingTheme)
        val ReadingFonts = IntKey(readingFonts)
        val ReadingAutoHideToolbar = BooleanKey(readingAutoHideToolbar)
        val ReadingTitleBold = BooleanKey(readingTitleBold)
        val ReadingSubheadBold = BooleanKey(readingSubheadBold)
        val ReadingTitleUpperCase = BooleanKey(readingTitleUpperCase)
        val ReadingSubheadUpperCase = BooleanKey(readingSubheadUpperCase)
        val ReadingImageMaximize = BooleanKey(readingImageMaximize)
        val ReadingImageHorizontalPadding = IntKey(readingImageHorizontalPadding)
        val ReadingImageRoundedCorners = IntKey(readingImageRoundedCorners)
        val InitialPage = IntKey(initialPage)
        val InitialFilter = IntKey(initialFilter)
        val SwipeStartAction = IntKey(swipeStartAction)
        val SwipeEndAction = IntKey(swipeEndAction)
        val MarkAsReadOnScroll = BooleanKey(markAsReadOnScroll)
        val HideEmptyGroups = BooleanKey(hideEmptyGroups)
        val PullToLoadNextFeed = BooleanKey(pullToLoadNextFeed)
        val PullToSwitchArticle = BooleanKey(pullToSwitchArticle)
        val OpenLink = IntKey(openLink)
        val OpenLinkAppSpecificBrowser = StringKey(openLinkAppSpecificBrowser)
        val SharedContent = IntKey(sharedContent)
        val Languages = IntKey(languages)

        private val keyList =
            listOf(
                // Version
                IsFirstLaunch,
                NewVersionPublishDate,
                NewVersionLog,
                NewVersionSizeString,
                NewVersionDownloadUrl,
                NewVersionNumber,
                SkipVersionNumber,
                CurrentAccountId,
                CurrentAccountType,
                ThemeIndex,
                CustomPrimaryColor,
                DarkTheme,
                AmoledDarkTheme,
                BasicFonts,
                // Feeds page
                FeedsFilterBarStyle,
                FeedsFilterBarPadding,
                FeedsFilterBarTonalElevation,
                FeedsTopBarTonalElevation,
                FeedsGroupListExpand,
                FeedsGroupListTonalElevation,
                // Flow page
                FlowFilterBarStyle,
                FlowFilterBarPadding,
                FlowFilterBarTonalElevation,
                FlowTopBarTonalElevation,
                FlowArticleListFeedIcon,
                FlowArticleListFeedName,
                FlowArticleListImage,
                FlowArticleListDesc,
                FlowArticleListTime,
                FlowArticleListDateStickyHeader,
                FlowArticleListTonalElevation,
                FlowArticleListReadIndicator,
                FlowSortUnreadArticles,
                // Reading page
                ReadingRenderer,
                ReadingBoldCharacters,
                ReadingPageTonalElevation,
                ReadingTextFontSize,
                ReadingTextLineHeight,
                ReadingTextLetterSpacing,
                ReadingTextHorizontalPadding,
                ReadingTextBold,
                ReadingTextAlign,
                ReadingTitleAlign,
                ReadingSubheadAlign,
                ReadingTheme,
                ReadingFonts,
                ReadingAutoHideToolbar,
                ReadingTitleBold,
                ReadingSubheadBold,
                ReadingTitleUpperCase,
                ReadingSubheadUpperCase,
                ReadingImageMaximize,
                ReadingImageHorizontalPadding,
                ReadingImageRoundedCorners,
                // Interaction
                InitialPage,
                InitialFilter,
                SwipeStartAction,
                SwipeEndAction,
                MarkAsReadOnScroll,
                HideEmptyGroups,
                PullToLoadNextFeed,
                PullToSwitchArticle,
                OpenLink,
                OpenLinkAppSpecificBrowser,
                SharedContent,
                Languages,
            )

        val keys = keyList.associateBy { it.key.name }
    }
}

// todo: remove
@Deprecated("Use the type-safe PreferencesKey instead")
@Suppress("ConstPropertyName")
data class DataStoreKey<T>(val key: Preferences.Key<T>, val type: Class<T>) {
    companion object {
        const val isFirstLaunch = "isFirstLaunch"
        const val newVersionPublishDate = "newVersionPublishDate"
        const val newVersionLog = "newVersionLog"
        const val newVersionSizeString = "newVersionSizeString"
        const val newVersionDownloadUrl = "newVersionDownloadUrl"
        const val newVersionNumber = "newVersionNumber"
        const val skipVersionNumber = "skipVersionNumber"
        const val currentAccountId = "currentAccountId"
        const val currentAccountType = "currentAccountType"
        const val themeIndex = "themeIndex"
        const val customPrimaryColor = "customPrimaryColor"
        const val darkTheme = "darkTheme"
        const val amoledDarkTheme = "amoledDarkTheme"
        const val basicFonts = "basicFonts"

        // Feeds page
        const val feedsFilterBarStyle = "feedsFilterBarStyle"
        const val feedsFilterBarPadding = "feedsFilterBarPadding"
        const val feedsFilterBarTonalElevation = "feedsFilterBarTonalElevation"
        const val feedsTopBarTonalElevation = "feedsTopBarTonalElevation"
        const val feedsGroupListExpand = "feedsGroupListExpand"
        const val feedsGroupListTonalElevation = "feedsGroupListTonalElevation"

        // Flow page
        const val flowFilterBarStyle = "flowFilterBarStyle"
        const val flowFilterBarPadding = "flowFilterBarPadding"
        const val flowFilterBarTonalElevation = "flowFilterBarTonalElevation"
        const val flowTopBarTonalElevation = "flowTopBarTonalElevation"
        const val flowArticleListFeedIcon = "flowArticleListFeedIcon"
        const val flowArticleListFeedName = "flowArticleListFeedName"
        const val flowArticleListImage = "flowArticleListImage"
        const val flowArticleListDesc = "flowArticleListDescription"
        const val flowArticleListTime = "flowArticleListTime"
        const val flowArticleListDateStickyHeader = "flowArticleListDateStickyHeader"
        const val flowArticleListTonalElevation = "flowArticleListTonalElevation"
        const val flowArticleListReadIndicator = "flowArticleListReadStatusIndicator"
        const val flowSortUnreadArticles = "flowArticleListSortUnreadArticles"

        // Reading page
        const val readingRenderer = "readingRender"
        const val readingBoldCharacters = "readingBoldCharacters"
        const val readingPageTonalElevation = "readingPageTonalElevation"
        const val readingTextFontSize = "readingTextFontSize"
        const val readingTextLineHeight = "readingTextLineHeight"
        const val readingTextLetterSpacing = "readingTextLetterSpacing"
        const val readingTextHorizontalPadding = "readingTextHorizontalPadding"
        const val readingTextBold = "readingTextBold"
        const val readingTextAlign = "readingTextAlign"
        const val readingTitleAlign = "readingTitleAlign"
        const val readingSubheadAlign = "readingSubheadAlign"
        const val readingTheme = "readingTheme"
        const val readingFonts = "readingFonts"
        const val readingAutoHideToolbar = "readingAutoHideToolbar"
        const val readingTitleBold = "readingTitleBold"
        const val readingSubheadBold = "readingSubheadBold"
        const val readingTitleUpperCase = "readingTitleUpperCase"
        const val readingSubheadUpperCase = "readingSubheadUpperCase"
        const val readingImageMaximize = "readingImageMaximize"
        const val readingImageHorizontalPadding = "readingImageHorizontalPadding"
        const val readingImageRoundedCorners = "readingImageRoundedCorners"

        // Interaction
        const val initialPage = "initialPage"
        const val initialFilter = "initialFilter"
        const val swipeStartAction = "swipeStartAction"
        const val swipeEndAction = "swipeEndAction"
        const val markAsReadOnScroll = "markAsReadOnScroll"
        const val hideEmptyGroups = "hideEmptyGroups"
        const val pullToLoadNextFeed = "pullToLoadNextFeed"
        const val pullToSwitchArticle = "pullToSwitchArticle"
        const val openLink = "openLink"
        const val openLinkAppSpecificBrowser = "openLinkAppSpecificBrowser"
        const val sharedContent = "sharedContent"

        // Languages
        const val languages = "languages"


        val keys: MutableMap<String, DataStoreKey<*>> =
            mutableMapOf(
                // Version
                isFirstLaunch to
                        DataStoreKey(booleanPreferencesKey(isFirstLaunch), Boolean::class.java),
                newVersionPublishDate to
                        DataStoreKey(stringPreferencesKey(newVersionPublishDate), String::class.java),
                newVersionLog to
                        DataStoreKey(stringPreferencesKey(newVersionLog), String::class.java),
                newVersionSizeString to
                        DataStoreKey(stringPreferencesKey(newVersionSizeString), String::class.java),
                newVersionDownloadUrl to
                        DataStoreKey(stringPreferencesKey(newVersionDownloadUrl), String::class.java),
                newVersionNumber to
                        DataStoreKey(stringPreferencesKey(newVersionNumber), String::class.java),
                skipVersionNumber to
                        DataStoreKey(stringPreferencesKey(skipVersionNumber), String::class.java),
                currentAccountId to
                        DataStoreKey(intPreferencesKey(currentAccountId), Int::class.java),
                currentAccountType to
                        DataStoreKey(intPreferencesKey(currentAccountType), Int::class.java),
                themeIndex to DataStoreKey(intPreferencesKey(themeIndex), Int::class.java),
                customPrimaryColor to
                        DataStoreKey(stringPreferencesKey(customPrimaryColor), String::class.java),
                darkTheme to DataStoreKey(intPreferencesKey(darkTheme), Int::class.java),
                amoledDarkTheme to
                        DataStoreKey(booleanPreferencesKey(amoledDarkTheme), Boolean::class.java),
                basicFonts to DataStoreKey(intPreferencesKey(basicFonts), Int::class.java),
                // Feeds page
                feedsFilterBarStyle to
                        DataStoreKey(intPreferencesKey(feedsFilterBarStyle), Int::class.java),
                feedsFilterBarPadding to
                        DataStoreKey(intPreferencesKey(feedsFilterBarPadding), Int::class.java),
                feedsFilterBarTonalElevation to
                        DataStoreKey(intPreferencesKey(feedsFilterBarTonalElevation), Int::class.java),
                feedsTopBarTonalElevation to
                        DataStoreKey(intPreferencesKey(feedsTopBarTonalElevation), Int::class.java),
                feedsGroupListExpand to
                        DataStoreKey(booleanPreferencesKey(feedsGroupListExpand), Boolean::class.java),
                feedsGroupListTonalElevation to
                        DataStoreKey(intPreferencesKey(feedsGroupListTonalElevation), Int::class.java),
                // Flow page
                flowFilterBarStyle to
                        DataStoreKey(intPreferencesKey(flowFilterBarStyle), Int::class.java),
                flowFilterBarPadding to
                        DataStoreKey(intPreferencesKey(flowFilterBarPadding), Int::class.java),
                flowFilterBarTonalElevation to
                        DataStoreKey(intPreferencesKey(flowFilterBarTonalElevation), Int::class.java),
                flowTopBarTonalElevation to
                        DataStoreKey(intPreferencesKey(flowTopBarTonalElevation), Int::class.java),
                flowArticleListFeedIcon to
                        DataStoreKey(
                            booleanPreferencesKey(flowArticleListFeedIcon),
                            Boolean::class.java,
                        ),
                flowArticleListFeedName to
                        DataStoreKey(
                            booleanPreferencesKey(flowArticleListFeedName),
                            Boolean::class.java,
                        ),
                flowArticleListImage to
                        DataStoreKey(booleanPreferencesKey(flowArticleListImage), Boolean::class.java),
                flowArticleListDesc to
                        DataStoreKey(booleanPreferencesKey(flowArticleListDesc), Boolean::class.java),
                flowArticleListTime to
                        DataStoreKey(booleanPreferencesKey(flowArticleListTime), Boolean::class.java),
                flowArticleListDateStickyHeader to
                        DataStoreKey(
                            booleanPreferencesKey(flowArticleListDateStickyHeader),
                            Boolean::class.java,
                        ),
                flowArticleListTonalElevation to
                        DataStoreKey(intPreferencesKey(flowArticleListTonalElevation), Int::class.java),
                flowArticleListReadIndicator to
                        DataStoreKey(intPreferencesKey(flowArticleListReadIndicator), Int::class.java),
                flowSortUnreadArticles to
                        DataStoreKey(
                            booleanPreferencesKey(flowSortUnreadArticles),
                            Boolean::class.java,
                        ),
                // Reading page
                readingRenderer to
                        DataStoreKey(intPreferencesKey(readingRenderer), Int::class.java),
                readingBoldCharacters to
                        DataStoreKey(booleanPreferencesKey(readingBoldCharacters), Boolean::class.java),
                readingPageTonalElevation to
                        DataStoreKey(intPreferencesKey(readingPageTonalElevation), Int::class.java),
                readingTextFontSize to
                        DataStoreKey(intPreferencesKey(readingTextFontSize), Int::class.java),
                readingTextLineHeight to
                        DataStoreKey(floatPreferencesKey(readingTextLineHeight), Float::class.java),
                readingTextLetterSpacing to
                        DataStoreKey(floatPreferencesKey(readingTextLetterSpacing), Float::class.java),
                readingTextHorizontalPadding to
                        DataStoreKey(intPreferencesKey(readingTextHorizontalPadding), Int::class.java),
                readingTextBold to
                        DataStoreKey(booleanPreferencesKey(readingTextBold), Boolean::class.java),
                readingTextAlign to
                        DataStoreKey(intPreferencesKey(readingTextAlign), Int::class.java),
                readingTitleAlign to
                        DataStoreKey(intPreferencesKey(readingTitleAlign), Int::class.java),
                readingSubheadAlign to
                        DataStoreKey(intPreferencesKey(readingSubheadAlign), Int::class.java),
                readingTheme to DataStoreKey(intPreferencesKey(readingTheme), Int::class.java),
                readingFonts to DataStoreKey(intPreferencesKey(readingFonts), Int::class.java),
                readingAutoHideToolbar to
                        DataStoreKey(
                            booleanPreferencesKey(readingAutoHideToolbar),
                            Boolean::class.java,
                        ),
                readingTitleBold to
                        DataStoreKey(booleanPreferencesKey(readingTitleBold), Boolean::class.java),
                readingSubheadBold to
                        DataStoreKey(booleanPreferencesKey(readingSubheadBold), Boolean::class.java),
                readingTitleUpperCase to
                        DataStoreKey(booleanPreferencesKey(readingTitleUpperCase), Boolean::class.java),
                readingSubheadUpperCase to
                        DataStoreKey(
                            booleanPreferencesKey(readingSubheadUpperCase),
                            Boolean::class.java,
                        ),
                readingImageMaximize to
                        DataStoreKey(booleanPreferencesKey(readingImageMaximize), Boolean::class.java),
                readingImageHorizontalPadding to
                        DataStoreKey(intPreferencesKey(readingImageHorizontalPadding), Int::class.java),
                readingImageRoundedCorners to
                        DataStoreKey(intPreferencesKey(readingImageRoundedCorners), Int::class.java),
                // Interaction
                initialPage to DataStoreKey(intPreferencesKey(initialPage), Int::class.java),
                initialFilter to DataStoreKey(intPreferencesKey(initialFilter), Int::class.java),
                swipeStartAction to
                        DataStoreKey(intPreferencesKey(swipeStartAction), Int::class.java),
                swipeEndAction to DataStoreKey(intPreferencesKey(swipeEndAction), Int::class.java),
                markAsReadOnScroll to
                        DataStoreKey(booleanPreferencesKey(markAsReadOnScroll), Boolean::class.java),
                hideEmptyGroups to
                        DataStoreKey(booleanPreferencesKey(hideEmptyGroups), Boolean::class.java),
                pullToLoadNextFeed to
                        DataStoreKey(booleanPreferencesKey(pullToLoadNextFeed), Boolean::class.java),
                pullToSwitchArticle to
                        DataStoreKey(booleanPreferencesKey(pullToSwitchArticle), Boolean::class.java),
                openLink to DataStoreKey(intPreferencesKey(openLink), Int::class.java),
                openLinkAppSpecificBrowser to
                        DataStoreKey(
                            stringPreferencesKey(openLinkAppSpecificBrowser),
                            String::class.java,
                        ),
                sharedContent to DataStoreKey(intPreferencesKey(sharedContent), Int::class.java),
                // Languages
                languages to DataStoreKey(intPreferencesKey(languages), Int::class.java),
            )
    }

}

val ignorePreferencesOnExportAndImport =
    listOf(
        DataStoreKey.currentAccountId,
        DataStoreKey.currentAccountType,
        DataStoreKey.isFirstLaunch,
    )

suspend fun Context.fromDataStoreToJSONString(): String {
    val preferences = dataStore.data.first()
    val map: Map<String, Any?> =
        preferences
            .asMap()
            .mapKeys { it.key.name }
            .filterKeys { it !in ignorePreferencesOnExportAndImport }
    return Gson().toJson(map)
}

suspend fun String.fromJSONStringToDataStore(context: Context) {
    val gson = Gson()
    val type = object : TypeToken<Map<String, *>>() {}.type
    val deserializedMap: Map<String, Any> = gson.fromJson(this, type)
    context.dataStore.edit { preferences ->
        deserializedMap
            .filterKeys { it !in ignorePreferencesOnExportAndImport }
            .forEach { (keyString, value) ->
                val preferenceKey = PreferenceKey.keys[keyString]
                when (preferenceKey) {
                    is PreferenceKey.BooleanKey -> {
                        if (value is Boolean) preferences[preferenceKey.key] = value
                    }
                    is PreferenceKey.FloatKey -> {
                        if (value is Number) preferences[preferenceKey.key] = value.toFloat()
                    }
                    is PreferenceKey.IntKey -> {
                        if (value is Number) preferences[preferenceKey.key] = value.toInt()
                    }
                    is PreferenceKey.LongKey -> {
                        if (value is Number) preferences[preferenceKey.key] = value.toLong()
                    }
                    is PreferenceKey.StringKey -> {
                        if (value is String) preferences[preferenceKey.key] = value
                    }
                    null -> return@forEach
                }
            }
    }
}

package me.ash.reader.ui.ext

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
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

suspend fun DataStore<Preferences>.put(dataStoreKeys: String, value: Any) {
    val key = DataStoreKey.keys[dataStoreKeys]?.key ?: return
    this.edit {
        withContext(Dispatchers.IO) {
            when (value) {
                is Int -> {
                    it[key as Preferences.Key<Int>] = value
                }
                is Long -> {
                    it[key as Preferences.Key<Long>] = value
                }
                is String -> {
                    it[key as Preferences.Key<String>] = value
                }
                is Boolean -> {
                    it[key as Preferences.Key<Boolean>] = value
                }
                is Float -> {
                    it[key as Preferences.Key<Float>] = value
                }
                is Double -> {
                    it[key as Preferences.Key<Double>] = value
                }
                else -> {
                    throw IllegalArgumentException("Unsupported type")
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> DataStore<Preferences>.get(key: String): T? {
    return runBlocking {
        this@get.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e("RLog", "Get data store error $exception")
                    exception.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { it[DataStoreKey.keys[key]?.key as Preferences.Key<T>] }
            .first() as T
    }
}

sealed interface PreferencesKey {
    val name: String
    val key: Preferences.Key<*>

    data class IntKey(
        override val name: String,
        override val key: Preferences.Key<Int> = intPreferencesKey(name),
    ) : PreferencesKey

    data class LongKey(
        override val name: String,
        override val key: Preferences.Key<Long> = longPreferencesKey(name),
    ) : PreferencesKey

    data class StringKey(
        override val name: String,
        override val key: Preferences.Key<String> = stringPreferencesKey(name),
    ) : PreferencesKey

    data class BooleanKey(
        override val name: String,
        override val key: Preferences.Key<Boolean> = booleanPreferencesKey(name),
    ) : PreferencesKey

    data class FloatKey(
        override val name: String,
        override val key: Preferences.Key<Float> = floatPreferencesKey(name),
    ) : PreferencesKey

    companion object {
        // Version
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

        // AI
        const val aiBaseUrl = "aiBaseUrl"
        const val aiApiKey = "aiApiKey"
        const val aiModel = "aiModel"
        const val aiSummarizationPrompt = "aiSummarizationPrompt"

        private val keyList =
            listOf(
                // Version
                BooleanKey(isFirstLaunch),
                StringKey(newVersionPublishDate),
                StringKey(newVersionLog),
                StringKey(newVersionSizeString),
                StringKey(newVersionDownloadUrl),
                StringKey(newVersionNumber),
                StringKey(skipVersionNumber),
                IntKey(currentAccountId),
                IntKey(currentAccountType),
                IntKey(themeIndex),
                StringKey(customPrimaryColor),
                IntKey(darkTheme),
                BooleanKey(amoledDarkTheme),
                IntKey(basicFonts),
                // Feeds page
                IntKey(feedsFilterBarStyle),
                IntKey(feedsFilterBarPadding),
                IntKey(feedsFilterBarTonalElevation),
                IntKey(feedsTopBarTonalElevation),
                BooleanKey(feedsGroupListExpand),
                IntKey(feedsGroupListTonalElevation),
                // Flow page
                IntKey(flowFilterBarStyle),
                IntKey(flowFilterBarPadding),
                IntKey(flowFilterBarTonalElevation),
                IntKey(flowTopBarTonalElevation),
                BooleanKey(flowArticleListFeedIcon),
                BooleanKey(flowArticleListFeedName),
                BooleanKey(flowArticleListImage),
                BooleanKey(flowArticleListDesc),
                BooleanKey(flowArticleListTime),
                BooleanKey(flowArticleListDateStickyHeader),
                IntKey(flowArticleListTonalElevation),
                IntKey(flowArticleListReadIndicator),
                BooleanKey(flowSortUnreadArticles),
                // Reading page
                IntKey(readingRenderer),
                BooleanKey(readingBoldCharacters),
                IntKey(readingPageTonalElevation),
                IntKey(readingTextFontSize),
                FloatKey(readingTextLineHeight),
                FloatKey(readingTextLetterSpacing),
                IntKey(readingTextHorizontalPadding),
                BooleanKey(readingTextBold),
                IntKey(readingTextAlign),
                IntKey(readingTitleAlign),
                IntKey(readingSubheadAlign),
                IntKey(readingTheme),
                IntKey(readingFonts),
                BooleanKey(readingAutoHideToolbar),
                BooleanKey(readingTitleBold),
                BooleanKey(readingSubheadBold),
                BooleanKey(readingTitleUpperCase),
                BooleanKey(readingSubheadUpperCase),
                BooleanKey(readingImageMaximize),
                IntKey(readingImageHorizontalPadding),
                IntKey(readingImageRoundedCorners),
                // Interaction
                IntKey(initialPage),
                IntKey(initialFilter),
                IntKey(swipeStartAction),
                IntKey(swipeEndAction),
                BooleanKey(markAsReadOnScroll),
                BooleanKey(hideEmptyGroups),
                BooleanKey(pullToLoadNextFeed),
                BooleanKey(pullToSwitchArticle),
                IntKey(openLink),
                StringKey(openLinkAppSpecificBrowser),
                IntKey(sharedContent),
                // Languages
                IntKey(languages),
                // AI
                StringKey(aiBaseUrl),
                StringKey(aiApiKey),
                StringKey(aiModel),
                StringKey(aiSummarizationPrompt),
            )

        val keys = keyList.associateBy { it.name }
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

        // AI
        const val aiBaseUrl = "aiBaseUrl"
        const val aiApiKey = "aiApiKey"
        const val aiModel = "aiModel"
        const val aiSummarizationPrompt = "aiSummarizationPrompt"

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
                // AI
                aiBaseUrl to DataStoreKey(stringPreferencesKey(aiBaseUrl), String::class.java),
                aiApiKey to DataStoreKey(stringPreferencesKey(aiApiKey), String::class.java),
                aiModel to DataStoreKey(stringPreferencesKey(aiModel), String::class.java),
                aiSummarizationPrompt to DataStoreKey(stringPreferencesKey(aiSummarizationPrompt), String::class.java),
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
                val preferencesKey = PreferencesKey.keys[keyString]
                when (preferencesKey) {
                    is PreferencesKey.BooleanKey -> {
                        if (value is Boolean) preferences[preferencesKey.key] = value
                    }
                    is PreferencesKey.FloatKey -> {
                        if (value is Number) preferences[preferencesKey.key] = value.toFloat()
                    }
                    is PreferencesKey.IntKey -> {
                        if (value is Number) preferences[preferencesKey.key] = value.toInt()
                    }
                    is PreferencesKey.LongKey -> {
                        if (value is Number) preferences[preferencesKey.key] = value.toLong()
                    }
                    is PreferencesKey.StringKey -> {
                        if (value is String) preferences[preferencesKey.key] = value
                    }
                    null -> return@forEach
                }
            }
    }
}

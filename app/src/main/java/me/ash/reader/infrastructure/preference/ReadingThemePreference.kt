package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTheme
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTheme =
    compositionLocalOf<ReadingThemePreference> { ReadingThemePreference.default }

@Immutable
sealed class ReadingThemePreference(val value: Int) : Preference() {

    object MaterialYou : ReadingThemePreference(0)
    object Reeder : ReadingThemePreference(1)
    object Paper : ReadingThemePreference(2)
    object Custom : ReadingThemePreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingTheme, value)
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            MaterialYou -> "Material You"
            Reeder -> "Reeder"
            Paper -> "Paper"
            Custom -> "Custom"
        }

    fun applyTheme(context: Context, scope: CoroutineScope) {
        when (this) {
            MaterialYou -> {
                ReadingTitleBoldPreference.default.put(context, scope)
                ReadingTitleUpperCasePreference.default.put(context, scope)
                ReadingTitleAlignPreference.default.put(context, scope)
                ReadingSubheadBoldPreference.default.put(context, scope)
                ReadingSubheadUpperCasePreference.default.put(context, scope)
                ReadingSubheadAlignPreference.default.put(context, scope)
                ReadingTextBoldPreference.default.put(context, scope)
                ReadingTextHorizontalPaddingPreference.put(context, scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(context, scope)
                ReadingTextLetterSpacingPreference.put(context, scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(context, scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(context, scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(context, scope, ReadingImageRoundedCornersPreference.default)
                ReadingImageHorizontalPaddingPreference.put(context, scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(context, scope)
            }

            Reeder -> {
                ReadingTitleBoldPreference.ON.put(context, scope)
                ReadingTitleUpperCasePreference.default.put(context, scope)
                ReadingTitleAlignPreference.default.put(context, scope)
                ReadingSubheadBoldPreference.ON.put(context, scope)
                ReadingSubheadUpperCasePreference.default.put(context, scope)
                ReadingSubheadAlignPreference.default.put(context, scope)
                ReadingTextBoldPreference.default.put(context, scope)
                ReadingTextHorizontalPaddingPreference.put(context, scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(context, scope)
                ReadingTextLetterSpacingPreference.put(context, scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(context, scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(context, scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(context, scope, 0)
                ReadingImageHorizontalPaddingPreference.put(context, scope, 0)
                ReadingImageMaximizePreference.default.put(context, scope)
            }

            Paper -> {
                ReadingTitleBoldPreference.ON.put(context, scope)
                ReadingTitleUpperCasePreference.ON.put(context, scope)
                ReadingTitleAlignPreference.Center.put(context, scope)
                ReadingSubheadBoldPreference.ON.put(context, scope)
                ReadingSubheadUpperCasePreference.ON.put(context, scope)
                ReadingSubheadAlignPreference.Center.put(context, scope)
                ReadingTextBoldPreference.default.put(context, scope)
                ReadingTextHorizontalPaddingPreference.put(context, scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.Center.put(context, scope)
                ReadingTextLetterSpacingPreference.put(context, scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(context, scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(context, scope, 20)
                ReadingImageRoundedCornersPreference.put(context, scope, 0)
                ReadingImageHorizontalPaddingPreference.put(context, scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(context, scope)
            }

            Custom -> {
                ReadingTitleBoldPreference.default.put(context, scope)
                ReadingTitleUpperCasePreference.default.put(context, scope)
                ReadingTitleAlignPreference.default.put(context, scope)
                ReadingSubheadBoldPreference.default.put(context, scope)
                ReadingSubheadUpperCasePreference.default.put(context, scope)
                ReadingSubheadAlignPreference.default.put(context, scope)
                ReadingTextBoldPreference.default.put(context, scope)
                ReadingTextHorizontalPaddingPreference.put(context, scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(context, scope)
                ReadingTextLetterSpacingPreference.put(context, scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(context, scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(context, scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(context, scope, ReadingImageRoundedCornersPreference.default)
                ReadingImageHorizontalPaddingPreference.put(context, scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(context, scope)
            }
        }
    }

    companion object {

        val default = Reeder
        val values = listOf(MaterialYou, Reeder, Paper, Custom)

        fun fromPreferences(preferences: Preferences): ReadingThemePreference =
            when (preferences[DataStoreKey.keys[readingTheme]?.key as Preferences.Key<Int>]) {
                0 -> MaterialYou
                1 -> Reeder
                2 -> Paper
                3 -> Custom
                else -> default
            }
    }
}

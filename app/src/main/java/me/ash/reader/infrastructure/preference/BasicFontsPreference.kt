package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.basicFonts
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.restart
import me.ash.reader.ui.theme.GoogleSansFontFamily
import me.ash.reader.ui.theme.SystemTypography
import me.ash.reader.ui.theme.applyFontFamily

val LocalBasicFonts = compositionLocalOf<BasicFontsPreference> { BasicFontsPreference.default }

sealed class BasicFontsPreference(val value: Int) : Preference() {
    object System : BasicFontsPreference(0)

    object GoogleSans : BasicFontsPreference(1)

    object External : BasicFontsPreference(5)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.basicFonts, value)
            if (this@BasicFontsPreference == External) {
                context.restart()
            }
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            System -> context.getString(R.string.system_default)
            GoogleSans -> context.getString(R.string.google_sans)
            External -> context.getString(R.string.external_fonts)
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> FontFamily.Default
            GoogleSans -> GoogleSansFontFamily
            External ->
                ExternalFonts.loadBasicTypography(context).displayLarge.fontFamily
                    ?: FontFamily.Default
        }

    fun asTypography(context: Context): Typography =
        when (this) {
            System -> SystemTypography
            GoogleSans -> SystemTypography.applyFontFamily(GoogleSansFontFamily)
            External -> ExternalFonts.loadBasicTypography(context)
        }

    companion object {

        val default = GoogleSans
        val values = listOf(GoogleSans, System, External)

        fun fromPreferences(preferences: Preferences): BasicFontsPreference =
            when (preferences[DataStoreKey.keys[basicFonts]?.key as Preferences.Key<Int>]) {
                0 -> System
                1 -> GoogleSans
                5 -> External
                else -> default
            }
    }
}

package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import me.ash.reader.R
import me.ash.reader.ui.ext.PreferenceKey
import me.ash.reader.ui.ext.get

val LocalDarkTheme = compositionLocalOf<DarkThemePreference> { DarkThemePreference.default }

sealed class DarkThemePreference(override val value: Int) :
    AppPreference.IntPreference {
    override val key: PreferenceKey.IntKey = Companion.key

    object UseDeviceTheme : DarkThemePreference(0)

    object ON : DarkThemePreference(1)

    object OFF : DarkThemePreference(2)

    fun toDesc(context: Context): String =
        when (this) {
            UseDeviceTheme -> context.getString(R.string.use_device_theme)
            ON -> context.getString(R.string.on)
            OFF -> context.getString(R.string.off)
        }

    @Composable
    @ReadOnlyComposable
    fun isDarkTheme(): Boolean =
        when (this) {
            UseDeviceTheme -> isSystemInDarkTheme()
            ON -> true
            OFF -> false
        }

    companion object : AppPreference.PreferenceCompanion {
        override val key: PreferenceKey.IntKey = PreferenceKey.DarkTheme

        override val default = UseDeviceTheme
        override val values = listOf(UseDeviceTheme, ON, OFF)

        override fun fromPreferences(preferences: Preferences) =
            when (preferences[key]) {
                0 -> UseDeviceTheme
                1 -> ON
                2 -> OFF
                else -> default
            }
    }
}

@Composable
operator fun DarkThemePreference.not(): DarkThemePreference =
    when (this) {
        DarkThemePreference.UseDeviceTheme ->
            if (isSystemInDarkTheme()) {
                DarkThemePreference.OFF
            } else {
                DarkThemePreference.ON
            }

        DarkThemePreference.ON -> DarkThemePreference.OFF
        DarkThemePreference.OFF -> DarkThemePreference.ON
    }

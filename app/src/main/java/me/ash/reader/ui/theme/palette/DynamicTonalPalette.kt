package me.ash.reader.ui.theme.palette

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import me.ash.reader.infrastructure.preference.LocalAmoledDarkTheme
import me.ash.reader.infrastructure.preference.LocalDarkTheme

@Composable
fun dynamicLightColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current
    return lightColorScheme(
        primary = palettes primary 40,
        onPrimary = palettes primary 100,
        primaryContainer = palettes primary 90,
        onPrimaryContainer = palettes primary 30,
        inversePrimary = palettes primary 80,
        secondary = palettes secondary 40,
        onSecondary = palettes secondary 100,
        secondaryContainer = palettes secondary 90,
        onSecondaryContainer = palettes secondary 30,
        tertiary = palettes tertiary 40,
        onTertiary = palettes tertiary 100,
        tertiaryContainer = palettes tertiary 90,
        onTertiaryContainer = palettes tertiary 30,
        background = palettes neutral 99,
        onBackground = palettes neutral 10,
        surface = palettes neutral 98,
        onSurface = palettes neutral 10,
        surfaceVariant = palettes neutralVariant 90,
        onSurfaceVariant = palettes neutralVariant 30,
        surfaceTint = palettes primary 40,
        inverseSurface = palettes neutral 20,
        inverseOnSurface = palettes neutral 95,
        outline = palettes neutralVariant 50,
        outlineVariant = palettes neutralVariant 80,
        surfaceBright = palettes neutral 98,
        surfaceDim = palettes neutral 87,
        surfaceContainerLowest = palettes neutral 100,
        surfaceContainerLow = palettes neutral 96,
        surfaceContainer = palettes neutral 94,
        surfaceContainerHigh = palettes neutral 92,
        surfaceContainerHighest = palettes neutral 90,
        primaryFixed = palettes primary 90,
        onPrimaryFixed = palettes primary 10,
        primaryFixedDim = palettes primary 90,
        onPrimaryFixedVariant = palettes primary 30,
        secondaryFixed = palettes secondary 90,
        onSecondaryFixed = palettes secondary 10,
        secondaryFixedDim = palettes secondary 90,
        onSecondaryFixedVariant = palettes secondary 30,
        tertiaryFixed = palettes tertiary 90,
        onTertiaryFixed = palettes tertiary 10,
        tertiaryFixedDim = palettes tertiary 90,
        onTertiaryFixedVariant = palettes tertiary 30
    )
}

@Composable
fun dynamicDarkColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current
    val useAmoledDarkTheme = LocalAmoledDarkTheme.current.value

    return darkColorScheme(
        primary = palettes primary 80,
        onPrimary = palettes primary 20,
        primaryContainer = palettes primary 30,
        onPrimaryContainer = palettes primary 90,
        inversePrimary = palettes primary 40,
        secondary = palettes secondary 80,
        onSecondary = palettes secondary 20,
        secondaryContainer = palettes secondary 30,
        onSecondaryContainer = palettes secondary 90,
        tertiary = palettes tertiary 80,
        onTertiary = palettes tertiary 20,
        tertiaryContainer = palettes tertiary 30,
        onTertiaryContainer = palettes tertiary 90,
        background = palettes neutral 10,
        onBackground = palettes neutral 90,
        surface = palettes neutral 6,
        onSurface = palettes neutral 90,
        surfaceVariant = palettes neutralVariant 30,
        onSurfaceVariant = palettes neutralVariant 80,
        surfaceTint = palettes primary 80,
        inverseSurface = palettes neutral 90,
        inverseOnSurface = palettes neutral 20,
        outline = palettes neutralVariant 60,
        outlineVariant = palettes neutralVariant 30,
        surfaceBright = palettes neutral 24,
        surfaceDim = palettes neutral 6,
        surfaceContainerLowest = palettes neutral 4,
        surfaceContainerLow = palettes neutral 10,
        surfaceContainer = palettes neutral 12,
        surfaceContainerHigh = palettes neutral 17,
        surfaceContainerHighest = palettes neutral 22,
        primaryFixed = palettes primary 90,
        onPrimaryFixed = palettes primary 10,
        primaryFixedDim = palettes primary 90,
        onPrimaryFixedVariant = palettes primary 30,
        secondaryFixed = palettes secondary 90,
        onSecondaryFixed = palettes secondary 10,
        secondaryFixedDim = palettes secondary 90,
        onSecondaryFixedVariant = palettes secondary 30,
        tertiaryFixed = palettes tertiary 90,
        onTertiaryFixed = palettes tertiary 10,
        tertiaryFixedDim = palettes tertiary 90,
        onTertiaryFixedVariant = palettes tertiary 30
    ).run {
        if (useAmoledDarkTheme) copy(
            surface = Color.Black,
            surfaceContainerHighest = palettes neutral 8,
            surfaceContainerHigh = palettes neutral 6,
            surfaceContainer = palettes neutral 4,
            surfaceContainerLow = palettes neutral 4,
            surfaceContainerLowest = Color.Black,
        ) else this
    }
}

@Composable
infix fun Color.onLight(lightColor: Color): Color =
    if (!LocalDarkTheme.current.isDarkTheme()) lightColor else this

@Composable
infix fun Color.onDark(darkColor: Color): Color =
    if (LocalDarkTheme.current.isDarkTheme()) darkColor else this

@Stable
@Composable
@ReadOnlyComposable
infix fun Color.alwaysLight(isAlways: Boolean): Color {
    val colorScheme = MaterialTheme.colorScheme
    return if (isAlways && LocalDarkTheme.current.isDarkTheme()) {
        when (this) {
            colorScheme.primary -> colorScheme.onPrimary
            colorScheme.secondary -> colorScheme.onSecondary
            colorScheme.tertiary -> colorScheme.onTertiary
            colorScheme.background -> colorScheme.onBackground
            colorScheme.error -> colorScheme.onError
            colorScheme.surface -> colorScheme.onSurface
            colorScheme.surfaceVariant -> colorScheme.onSurfaceVariant
            colorScheme.primaryContainer -> colorScheme.onPrimaryContainer
            colorScheme.secondaryContainer -> colorScheme.onSecondaryContainer
            colorScheme.tertiaryContainer -> colorScheme.onTertiaryContainer
            colorScheme.errorContainer -> colorScheme.onErrorContainer
            colorScheme.inverseSurface -> colorScheme.inverseOnSurface

            colorScheme.onPrimary -> colorScheme.primary
            colorScheme.onSecondary -> colorScheme.secondary
            colorScheme.onTertiary -> colorScheme.tertiary
            colorScheme.onBackground -> colorScheme.background
            colorScheme.onError -> colorScheme.error
            colorScheme.onSurface -> colorScheme.surface
            colorScheme.onSurfaceVariant -> colorScheme.surfaceVariant
            colorScheme.onPrimaryContainer -> colorScheme.primaryContainer
            colorScheme.onSecondaryContainer -> colorScheme.secondaryContainer
            colorScheme.onTertiaryContainer -> colorScheme.tertiaryContainer
            colorScheme.onErrorContainer -> colorScheme.errorContainer
            colorScheme.inverseOnSurface -> colorScheme.inverseSurface

            else -> Color.Unspecified
        }
    } else {
        this
    }
}

@Stable
@Composable
@ReadOnlyComposable
infix fun Color.alwaysDark(isAlways: Boolean): Color {
    val colorScheme = MaterialTheme.colorScheme
    return if (isAlways && !LocalDarkTheme.current.isDarkTheme()) {
        when (this) {
            colorScheme.primary -> colorScheme.onPrimary
            colorScheme.secondary -> colorScheme.onSecondary
            colorScheme.tertiary -> colorScheme.onTertiary
            colorScheme.background -> colorScheme.onBackground
            colorScheme.error -> colorScheme.onError
            colorScheme.surface -> colorScheme.onSurface
            colorScheme.surfaceVariant -> colorScheme.onSurfaceVariant
            colorScheme.primaryContainer -> colorScheme.onPrimaryContainer
            colorScheme.secondaryContainer -> colorScheme.onSecondaryContainer
            colorScheme.tertiaryContainer -> colorScheme.onTertiaryContainer
            colorScheme.errorContainer -> colorScheme.onErrorContainer
            colorScheme.inverseSurface -> colorScheme.inverseOnSurface

            colorScheme.onPrimary -> colorScheme.primary
            colorScheme.onSecondary -> colorScheme.secondary
            colorScheme.onTertiary -> colorScheme.tertiary
            colorScheme.onBackground -> colorScheme.background
            colorScheme.onError -> colorScheme.error
            colorScheme.onSurface -> colorScheme.surface
            colorScheme.onSurfaceVariant -> colorScheme.surfaceVariant
            colorScheme.onPrimaryContainer -> colorScheme.primaryContainer
            colorScheme.onSecondaryContainer -> colorScheme.secondaryContainer
            colorScheme.onTertiaryContainer -> colorScheme.tertiaryContainer
            colorScheme.onErrorContainer -> colorScheme.errorContainer
            colorScheme.inverseOnSurface -> colorScheme.inverseSurface

            else -> Color.Unspecified
        }
    } else {
        this
    }
}

fun String.checkColorHex(): String? {
    var s = this.trim()
    if (s.length > 6) {
        s = s.substring(s.length - 6)
    }
    return "[0-9a-fA-F]{6}".toRegex().find(s)?.value
}

@Stable
fun String.safeHexToColor(): Color =
    try {
        Color(java.lang.Long.parseLong(this, 16))
    } catch (e: Exception) {
        Color.Transparent
    }

/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.theme.palette

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import me.ash.reader.ui.theme.palette.colorspace.cielab.CieLab
import me.ash.reader.ui.theme.palette.colorspace.zcam.Izazbz.Companion.toIzazbz
import me.ash.reader.ui.theme.palette.core.*

/**
 * The L from CIELab
 */
typealias TonalValue = Int

typealias TonalPalette = MutableMap<TonalValue, Color>

val tonalTokens = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100)

val LocalTonalPalettes = compositionLocalOf {
    TonalPalettes(238.36, 15.0)
}

@Composable
private fun TonalValue.toZcamLightness(): Double =
    CieLab(L = if (this != 50) toDouble() else 49.6, a = 0.0, b = 0.0).toXyz().toIzazbz()
        .toZcam().Jz

data class TonalPalettes(
    val hue: Double,
    val primaryChroma: Double,
    val primary: TonalPalette = mutableMapOf(),
    val secondary: TonalPalette = mutableMapOf(),
    val tertiary: TonalPalette = mutableMapOf(),
    val neutral: TonalPalette = mutableMapOf(),
    val neutralVariant: TonalPalette = mutableMapOf(),
    val error: TonalPalette = mutableMapOf(),
) {

    @Composable
    infix fun primary(tone: TonalValue): Color = primary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = (1.2 * primaryChroma / MaterialYouStandard.sRGBLightnessChromaMap.maxOf { it.value })
                .coerceAtLeast(1.0) * MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone),
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun secondary(tone: TonalValue): Color = secondary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 3.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun tertiary(tone: TonalValue): Color = tertiary.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) * 2.0 / 3.0,
            h = hue + 60.0,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun neutral(tone: TonalValue): Color = neutral.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 8.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun neutralVariant(tone: TonalValue): Color = neutralVariant.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone) / 6.0,
            h = hue,
        ).clampToRgb().toColor()
    }

    @Composable
    infix fun error(tone: TonalValue): Color = error.getOrPut(tone) {
        zcamLch(
            L = tone.toZcamLightness(),
            C = MaterialYouStandard.sRGBLightnessChromaMap.getValue(tone),
            h = 33.44,
        ).clampToRgb().toColor()
    }

    @Composable
    fun Preparing() {
        tonalTokens.forEach { primary(it) }
        tonalTokens.forEach { secondary(it) }
        tonalTokens.forEach { tertiary(it) }
        tonalTokens.forEach { neutral(it) }
        tonalTokens.forEach { neutralVariant(it) }
        tonalTokens.forEach { error(it) }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun primarySystem(context: Context, tone: TonalValue): Color = when (tone) {
        0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_1000)
        10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_900)
        20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_800)
        30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_700)
        40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_600)
        50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_500)
        60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_400)
        70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_300)
        80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_200)
        90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_100)
        95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_50)
        99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_10)
        100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent1_0)
        else -> throw IllegalArgumentException("Unknown primary tone: $tone")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun secondarySystem(context: Context, tone: TonalValue): Color = when (tone) {
        0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_1000)
        10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_900)
        20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_800)
        30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_700)
        40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_600)
        50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_500)
        60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_400)
        70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_300)
        80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_200)
        90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_100)
        95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_50)
        99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_10)
        100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent2_0)
        else -> throw IllegalArgumentException("Unknown secondary tone: $tone")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun tertiarySystem(context: Context, tone: TonalValue): Color = when (tone) {
        0 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_1000)
        10 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_900)
        20 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_800)
        30 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_700)
        40 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_600)
        50 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_500)
        60 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_400)
        70 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_300)
        80 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_200)
        90 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_100)
        95 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_50)
        99 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_10)
        100 -> ColorResourceHelper.getColor(context, android.R.color.system_accent3_0)
        else -> throw IllegalArgumentException("Unknown tertiary tone: $tone")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun neutralSystem(context: Context, tone: TonalValue): Color = when (tone) {
        0 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_1000)
        10 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_900)
        20 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_800)
        30 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_700)
        40 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_600)
        50 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_500)
        60 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_400)
        70 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_300)
        80 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_200)
        90 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_100)
        95 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_50)
        99 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_10)
        100 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral1_0)
        else -> throw IllegalArgumentException("Unknown neutral tone: $tone")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun neutralVariantSystem(context: Context, tone: TonalValue): Color = when (tone) {
        0 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_1000)
        10 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_900)
        20 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_800)
        30 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_700)
        40 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_600)
        50 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_500)
        60 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_400)
        70 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_300)
        80 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_200)
        90 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_100)
        95 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_50)
        99 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_10)
        100 -> ColorResourceHelper.getColor(context, android.R.color.system_neutral2_0)
        else -> throw IllegalArgumentException("Unknown neutral variant tone: $tone")
    }

    companion object {

        @Composable
        @Stable
        fun Color.toTonalPalettes(): TonalPalettes {
            val zcam = toRgb().toZcam()
            return TonalPalettes(hue = zcam.hz, primaryChroma = zcam.Cz)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @Composable
        @Stable
        fun Context.getSystemTonalPalettes(): TonalPalettes {
            val tonalPalettes = TonalPalettes(238.36, 15.0)
            tonalTokens.forEach {
                tonalPalettes.primary[it] = tonalPalettes.primarySystem(this, it)
            }
            tonalTokens.forEach {
                tonalPalettes.secondary[it] = tonalPalettes.secondarySystem(this, it)
            }
            tonalTokens.forEach {
                tonalPalettes.tertiary[it] = tonalPalettes.tertiarySystem(this, it)
            }
            tonalTokens.forEach {
                tonalPalettes.neutral[it] = tonalPalettes.neutralSystem(this, it)
            }
            tonalTokens.forEach {
                tonalPalettes.neutralVariant[it] = tonalPalettes.neutralVariantSystem(this, it)
            }
            tonalTokens.forEach {
                tonalPalettes error it
            }
            return tonalPalettes
        }
    }
}

@RequiresApi(23)
object ColorResourceHelper {

    @DoNotInline
    fun getColor(context: Context, @ColorRes id: Int): Color {
        return Color(context.resources.getColor(id, context.theme))
    }
}

/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.core

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import me.ash.reader.ui.theme.palette.colorspace.rgb.Rgb
import me.ash.reader.ui.theme.palette.colorspace.rgb.RgbColorSpace

fun Rgb.toColor(): Color = if (!r.isNaN() && !g.isNaN() && !b.isNaN())
    Color(
        red = r.toFloat(),
        green = g.toFloat(),
        blue = b.toFloat(),
        colorSpace = when (colorSpace) {
            RgbColorSpace.Srgb -> ColorSpaces.Srgb
            RgbColorSpace.DisplayP3 -> ColorSpaces.DisplayP3
            RgbColorSpace.BT2020 -> ColorSpaces.Bt2020
            else -> ColorSpaces.Srgb
        }
    ) else Color.Black

@Composable
fun Color.toRgb(): Rgb {
    val color = convert(
        when (LocalRgbColorSpace.current) {
            RgbColorSpace.Srgb -> ColorSpaces.Srgb
            RgbColorSpace.DisplayP3 -> ColorSpaces.DisplayP3
            RgbColorSpace.BT2020 -> ColorSpaces.Bt2020
            else -> ColorSpaces.Srgb
        }
    )
    return Rgb(
        r = color.red.toDouble(),
        g = color.green.toDouble(),
        b = color.blue.toDouble(),
        colorSpace = LocalRgbColorSpace.current
    )
}

@Composable
fun animateZcamLchAsState(
    targetValue: ZcamLch,
    animationSpec: AnimationSpec<ZcamLch> = spring(),
    finishedListener: ((ZcamLch) -> Unit)? = null,
): State<ZcamLch> {
    val converter = remember {
        TwoWayConverter<ZcamLch, AnimationVector3D>(
            convertToVector = {
                AnimationVector3D(it.L.toFloat(), it.C.toFloat(), it.h.toFloat())
            },
            convertFromVector = {
                ZcamLch(L = it.v1.toDouble(), C = it.v2.toDouble(), h = it.v3.toDouble())
            }
        )
    }
    return animateValueAsState(
        targetValue,
        converter,
        animationSpec,
        finishedListener = finishedListener
    )
}

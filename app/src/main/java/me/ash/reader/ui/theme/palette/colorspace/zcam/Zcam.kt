/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package me.ash.reader.ui.theme.palette.colorspace.zcam

import me.ash.reader.ui.theme.palette.colorspace.ciexyz.CieXyz
import me.ash.reader.ui.theme.palette.colorspace.rgb.Rgb
import me.ash.reader.ui.theme.palette.colorspace.rgb.Rgb.Companion.toRgb
import me.ash.reader.ui.theme.palette.colorspace.rgb.RgbColorSpace
import me.ash.reader.ui.theme.palette.colorspace.zcam.Izazbz.Companion.toIzazbz
import me.ash.reader.ui.theme.palette.util.square
import me.ash.reader.ui.theme.palette.util.toDegrees
import me.ash.reader.ui.theme.palette.util.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Zcam(
    val hz: Double = Double.NaN,
    val Qz: Double = Double.NaN,
    val Jz: Double = Double.NaN,
    val Mz: Double = Double.NaN,
    val Cz: Double = Double.NaN,
    val Sz: Double = Double.NaN,
    val Vz: Double = Double.NaN,
    val Kz: Double = Double.NaN,
    val Wz: Double = Double.NaN,
    val cond: ViewingConditions,
) {

    fun toIzazbz(): Izazbz {
        require(!hz.isNaN()) { "Must provide hz." }
        require(!Qz.isNaN() || !Jz.isNaN()) { "Must provide Qz or Jz." }
        require(!Mz.isNaN() || !Cz.isNaN() || !Sz.isNaN() || !Vz.isNaN() || !Kz.isNaN() || !Wz.isNaN()) {
            "Must provide Mz, Cz, Sz, Vz, Kz or Wz."
        }
        with(cond) {
            val Iz = (
                    when {
                        !Qz.isNaN() -> Qz
                        !Jz.isNaN() -> Jz * Qzw / 100.0
                        else -> Double.NaN
                    } / (2700.0 * F_s.pow(2.2) * sqrt(F_b) * F_L.pow(0.2))
                    ).pow(F_b.pow(0.12) / (1.6 * F_s))
            val Jz = Jz.takeUnless { it.isNaN() } ?: when {
                !Qz.isNaN() -> 100.0 * Qz / Qzw
                else -> Double.NaN
            }
            val Qz = Qz.takeUnless { it.isNaN() } ?: when {
                !Jz.isNaN() -> Jz * Qzw / 100.0
                else -> Double.NaN
            }
            val Cz = Cz.takeUnless { it.isNaN() } ?: when {
                !Sz.isNaN() -> Qz * square(Sz) / (100.0 * Qzw * F_L.pow(1.2))
                !Vz.isNaN() -> sqrt((square(Vz) - square(Jz - 58.0)) / 3.4)
                !Kz.isNaN() -> sqrt((square((100.0 - Kz) / 0.8) - square(Jz)) / 8.0)
                !Wz.isNaN() -> sqrt(square(100.0 - Wz) - square(100.0 - Jz))
                else -> Double.NaN
            }
            val Mz = Mz.takeUnless { it.isNaN() } ?: (Cz * Qzw / 100.0)

            val ez = 1.015 + cos(89.038 + hz).toRadians()
            val Cz_ =
                (Mz * Izw.pow(0.78) * F_b.pow(0.1) / (100.0 * ez.pow(0.068) * F_L.pow(0.2))).pow(1.0 / (0.37 * 2.0))
            val hzRad = hz.toRadians()
            val az = Cz_ * cos(hzRad)
            val bz = Cz_ * sin(hzRad)

            return Izazbz(
                Iz = Iz,
                az = az,
                bz = bz,
            )
        }
    }

    fun clampToRgb(colorSpace: RgbColorSpace): Rgb = toIzazbz()
        .toXyz()
        .toRgb(cond.luminance, colorSpace)
        .takeIf { it.isInGamut() }
        ?: copy(Cz = findChromaBoundaryInRgb(colorSpace, 0.001))
            .toIzazbz()
            .toXyz()
            .toRgb(cond.luminance, colorSpace)
            .clamp()

    private fun findChromaBoundaryInRgb(
        colorSpace: RgbColorSpace,
        error: Double,
    ): Double = chromaBoundary.getOrPut(Triple(colorSpace.hashCode(), hz, Jz)) {
        var low = 0.0
        var high = Cz
        var current = this
        while (high - low >= error) {
            val mid = (low + high) / 2.0
            current = copy(Cz = mid)
            if (!current.toIzazbz().toXyz().toRgb(cond.luminance, colorSpace).isInGamut()) {
                high = mid
            } else {
                val next = current.copy(Cz = mid + error).toIzazbz().toXyz()
                    .toRgb(cond.luminance, colorSpace)
                if (next.isInGamut()) {
                    low = mid
                } else {
                    break
                }
            }
        }
        current.Cz
    }

    companion object {

        private val chromaBoundary: MutableMap<Triple<Int, Double, Double>, Double> = mutableMapOf()

        data class ViewingConditions(
            val whitePoint: CieXyz,
            val luminance: Double,
            val F_s: Double,
            val L_a: Double,
            val Y_b: Double,
        ) {

            private val absoluteWhitePoint = whitePoint * luminance
            private val Y_w = absoluteWhitePoint.luminance
            val F_b = sqrt(Y_b / Y_w)
            val F_L = 0.171 * L_a.pow(1.0 / 3.0) * (1 - exp(-48.0 / 9.0 * L_a))
            val Izw = absoluteWhitePoint.toIzazbz().Iz
            val Qzw =
                2700.0 * Izw.pow(1.6 * F_s / F_b.pow(0.12)) * F_s.pow(2.2) * sqrt(F_b) * F_L.pow(0.2)
        }

        fun Izazbz.toZcam(cond: ViewingConditions): Zcam {
            with(cond) {
                val hz = atan2(bz, az).toDegrees().mod(360.0) // hue angle
                val Qz =
                    2700.0 * Iz.pow(1.6 * F_s / F_b.pow(0.12)) * F_s.pow(2.2) * sqrt(F_b) * F_L.pow(
                        0.2
                    ) // brightness
                val Jz = 100.0 * Qz / Qzw // lightness
                val ez = 1.015 + cos(89.038 + hz).toRadians() // ~ eccentricity factor
                val Mz =
                    100.0 * (square(az) + square(bz)).pow(0.37) * ez.pow(0.068) * F_L.pow(0.2) /
                            (F_b.pow(0.1) * Izw.pow(0.78)) // colorfulness
                val Cz = 100.0 * Mz / Qzw // chroma

                val Sz = 100.0 * F_L.pow(0.6) * sqrt(Mz / Qz) // saturation
                val Vz = sqrt(square(Jz - 58.0) + 3.4 * square(Cz)) // vividness
                val Kz = 100.0 - 0.8 * sqrt(square(Jz) + 8.0 * square(Cz)) // blackness
                val Wz = 100.0 - sqrt(square(100.0 - Jz) + square(Cz)) // blackness

                return Zcam(
                    hz = hz,
                    Qz = Qz,
                    Jz = Jz,
                    Mz = Mz,
                    Cz = Cz,
                    Sz = Sz,
                    Vz = Vz,
                    Kz = Kz,
                    Wz = Wz,
                    cond = cond,
                )
            }
        }
    }
}

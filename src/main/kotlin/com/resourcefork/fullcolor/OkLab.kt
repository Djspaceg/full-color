package com.resourcefork.fullcolor

import kotlin.math.*

/**
 * OKLab color space representation.
 *
 * OKLab is a perceptual color space developed by Björn Ottosson that provides
 * uniform perceptual scaling and is suitable for color manipulation, gradients,
 * and analysis. L is lightness (0–1), a and b are opponent color axes.
 */
data class OkLab(
    val L: Float,
    val a: Float,
    val b: Float,
) {
    /** Convert OKLab to OKLch (cylindrical representation). */
    fun toOkLch(): OkLch {
        val chroma = sqrt(a * a + b * b)
        val hue = (Math.toDegrees(atan2(b.toDouble(), a.toDouble())).toFloat() + 360f) % 360f
        return OkLch(L, chroma, hue)
    }

    /** Linearly interpolate between this color and [other] in OKLab space. */
    fun lerp(other: OkLab, t: Float): OkLab = OkLab(
        L = L + (other.L - L) * t,
        a = a + (other.a - a) * t,
        b = b + (other.b - b) * t,
    )

    companion object {
        /** Convert linear-light sRGB components (0–1) to OKLab. */
        internal fun fromLinearRgb(r: Float, g: Float, b: Float): OkLab {
            val l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b
            val m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b
            val s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b

            val lCbrt = cbrt(l.toDouble()).toFloat()
            val mCbrt = cbrt(m.toDouble()).toFloat()
            val sCbrt = cbrt(s.toDouble()).toFloat()

            return OkLab(
                L = 0.2104542553f * lCbrt + 0.7936177850f * mCbrt - 0.0040720468f * sCbrt,
                a = 1.9779984951f * lCbrt - 2.4285922050f * mCbrt + 0.4505937099f * sCbrt,
                b = 0.0259040371f * lCbrt + 0.7827717662f * mCbrt - 0.8086757660f * sCbrt,
            )
        }

        /** Convert OKLab to linear-light sRGB components (0–1), unclamped. */
        internal fun toLinearRgb(lab: OkLab): Triple<Float, Float, Float> {
            val lCbrt = lab.L + 0.3963377774f * lab.a + 0.2158037573f * lab.b
            val mCbrt = lab.L - 0.1055613458f * lab.a - 0.0638541728f * lab.b
            val sCbrt = lab.L - 0.0894841775f * lab.a - 1.2914855480f * lab.b

            val l = lCbrt * lCbrt * lCbrt
            val m = mCbrt * mCbrt * mCbrt
            val s = sCbrt * sCbrt * sCbrt

            return Triple(
                4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s,
                -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s,
                -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s,
            )
        }
    }
}

/**
 * OKLch color space representation (cylindrical form of OKLab).
 *
 * L is lightness (0–1), C is chroma (≥ 0), H is hue angle in degrees (0–360).
 */
data class OkLch(
    val L: Float,
    val C: Float,
    val H: Float,
) {
    /** Convert OKLch back to OKLab. */
    fun toOkLab(): OkLab {
        val hRad = Math.toRadians(H.toDouble()).toFloat()
        return OkLab(
            L = L,
            a = C * cos(hRad),
            b = C * sin(hRad),
        )
    }

    /** Linearly interpolate between this and [other] in OKLch space (hue interpolated via shortest path). */
    fun lerp(other: OkLch, t: Float): OkLch {
        val dH = ((other.H - H + 540f) % 360f) - 180f
        return OkLch(
            L = L + (other.L - L) * t,
            C = C + (other.C - C) * t,
            H = (H + dH * t + 360f) % 360f,
        )
    }
}

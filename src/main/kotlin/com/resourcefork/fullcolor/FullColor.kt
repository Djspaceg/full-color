package com.resourcefork.fullcolor

import kotlin.math.*

/**
 * The central color object for the full-color library.
 *
 * Internally backed by the [OkLab] wide-gamut perceptual color space, [FullColor]
 * accepts inputs from many common color formats and can emit outputs in those same
 * formats. All manipulation (lighten, darken, rotate, mix …) takes place in OKLab
 * space, giving perceptually uniform results.
 *
 * Alpha is stored separately as a float in [0, 1].
 */
class FullColor private constructor(
    internal val lab: OkLab,
    val alpha: Float = 1f,
) {

    // ── Input factories ────────────────────────────────────────────────────────

    companion object {

        // ── sRGB helpers ───────────────────────────────────────────────────────

        /** Gamma-encode a linear-light component (0–1) to sRGB. */
        private fun linearToSrgb(c: Float): Float = when {
            c <= 0.0031308f -> 12.92f * c
            else -> (1.055f * c.toDouble().pow(1.0 / 2.4) - 0.055).toFloat()
        }

        /** Gamma-decode an sRGB component (0–1) to linear light. */
        private fun srgbToLinear(c: Float): Float = when {
            c <= 0.04045f -> c / 12.92f
            else -> ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }

        private fun Float.clamp01(): Float = coerceIn(0f, 1f)

        // ── Construction from OKLab ────────────────────────────────────────────

        /** Create a [FullColor] directly from an [OkLab] value. */
        fun fromOkLab(lab: OkLab, alpha: Float = 1f): FullColor =
            FullColor(lab, alpha.coerceIn(0f, 1f))

        /** Create a [FullColor] directly from an [OkLch] value. */
        fun fromOkLch(lch: OkLch, alpha: Float = 1f): FullColor =
            FullColor(lch.toOkLab(), alpha.coerceIn(0f, 1f))

        // ── Construction from sRGB ─────────────────────────────────────────────

        /**
         * Create from integer sRGB components (0–255 each).
         * This is compatible with `android.graphics.Color.rgb(r, g, b)`.
         */
        fun fromRgb(r: Int, g: Int, b: Int): FullColor = fromRgba(r, g, b, 255)

        /**
         * Create from integer sRGBA components (0–255 each).
         */
        fun fromRgba(r: Int, g: Int, b: Int, a: Int): FullColor {
            val rf = srgbToLinear((r.coerceIn(0, 255) / 255f))
            val gf = srgbToLinear((g.coerceIn(0, 255) / 255f))
            val bf = srgbToLinear((b.coerceIn(0, 255) / 255f))
            return FullColor(
                OkLab.fromLinearRgb(rf, gf, bf),
                a.coerceIn(0, 255) / 255f,
            )
        }

        /**
         * Create from floating-point sRGB components (0.0–1.0 each).
         */
        fun fromRgbFloat(r: Float, g: Float, b: Float, a: Float = 1f): FullColor {
            val rf = srgbToLinear(r.clamp01())
            val gf = srgbToLinear(g.clamp01())
            val bf = srgbToLinear(b.clamp01())
            return FullColor(OkLab.fromLinearRgb(rf, gf, bf), a.clamp01())
        }

        // ── Construction from hex strings ──────────────────────────────────────

        /**
         * Create from a CSS-style hex string.
         *
         * Supported formats: `#RGB`, `#RGBA`, `#RRGGBB`, `#RRGGBBAA`.
         * The leading `#` is optional.
         */
        fun fromHex(hex: String): FullColor {
            val s = hex.trimStart('#')
            return when (s.length) {
                3 -> fromRgb(
                    r = s[0].digitToInt(16) * 17,
                    g = s[1].digitToInt(16) * 17,
                    b = s[2].digitToInt(16) * 17,
                )
                4 -> fromRgba(
                    r = s[0].digitToInt(16) * 17,
                    g = s[1].digitToInt(16) * 17,
                    b = s[2].digitToInt(16) * 17,
                    a = s[3].digitToInt(16) * 17,
                )
                6 -> fromRgb(
                    r = s.substring(0, 2).toInt(16),
                    g = s.substring(2, 4).toInt(16),
                    b = s.substring(4, 6).toInt(16),
                )
                8 -> fromRgba(
                    r = s.substring(0, 2).toInt(16),
                    g = s.substring(2, 4).toInt(16),
                    b = s.substring(4, 6).toInt(16),
                    a = s.substring(6, 8).toInt(16),
                )
                else -> throw IllegalArgumentException(
                    "Unsupported hex color format: $hex. Expected #RGB, #RGBA, #RRGGBB, or #RRGGBBAA.",
                )
            }
        }

        // ── Construction from HSL / HSV ────────────────────────────────────────

        /**
         * Create from HSL values.
         * @param h Hue in degrees [0, 360).
         * @param s Saturation in [0, 1].
         * @param l Lightness in [0, 1].
         * @param a Alpha in [0, 1].
         */
        fun fromHsl(h: Float, s: Float, l: Float, a: Float = 1f): FullColor {
            val hNorm = ((h % 360f) + 360f) % 360f
            val sClamp = s.clamp01()
            val lClamp = l.clamp01()

            val c = (1f - abs(2f * lClamp - 1f)) * sClamp
            val x = c * (1f - abs((hNorm / 60f) % 2f - 1f))
            val m = lClamp - c / 2f

            val (r1, g1, b1) = when {
                hNorm < 60f -> Triple(c, x, 0f)
                hNorm < 120f -> Triple(x, c, 0f)
                hNorm < 180f -> Triple(0f, c, x)
                hNorm < 240f -> Triple(0f, x, c)
                hNorm < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            return fromRgbFloat(r1 + m, g1 + m, b1 + m, a.clamp01())
        }

        /**
         * Create from HSV (HSB) values.
         * @param h Hue in degrees [0, 360).
         * @param s Saturation in [0, 1].
         * @param v Value/Brightness in [0, 1].
         * @param a Alpha in [0, 1].
         */
        fun fromHsv(h: Float, s: Float, v: Float, a: Float = 1f): FullColor {
            val hNorm = ((h % 360f) + 360f) % 360f
            val sClamp = s.clamp01()
            val vClamp = v.clamp01()

            val c = vClamp * sClamp
            val x = c * (1f - abs((hNorm / 60f) % 2f - 1f))
            val m = vClamp - c

            val (r1, g1, b1) = when {
                hNorm < 60f -> Triple(c, x, 0f)
                hNorm < 120f -> Triple(x, c, 0f)
                hNorm < 180f -> Triple(0f, c, x)
                hNorm < 240f -> Triple(0f, x, c)
                hNorm < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            return fromRgbFloat(r1 + m, g1 + m, b1 + m, a.clamp01())
        }

        // ── Construction from Android / Compose packed integers ───────────────

        /**
         * Create from an Android-style packed ARGB integer (e.g. `0xFFFF0000` for red).
         * Compatible with `android.graphics.Color` values.
         */
        fun fromAndroidArgb(argb: Int): FullColor = fromRgba(
            r = (argb shr 16) and 0xFF,
            g = (argb shr 8) and 0xFF,
            b = argb and 0xFF,
            a = (argb ushr 24) and 0xFF,
        )

        /**
         * Create from a Jetpack Compose-style packed color long.
         *
         * Jetpack Compose stores sRGB colors as a 32-bit ARGB value (`0xAARRGGBB`)
         * in the lower 32 bits of the packed `Long`. This helper interprets the
         * lower 32 bits as an Android-style ARGB integer and delegates to
         * [fromAndroidArgb], ignoring any additional high bits used by Compose
         * for extended color spaces.
         */
        fun fromComposeColor(packed: Long): FullColor {
            // Always interpret the lower 32 bits as 0xAARRGGBB.
            return fromAndroidArgb(packed.toInt())
        }

        // ── Named/CSS color constants ──────────────────────────────────────────

        val BLACK: FullColor get() = fromRgb(0, 0, 0)
        val WHITE: FullColor get() = fromRgb(255, 255, 255)
        val RED: FullColor get() = fromRgb(255, 0, 0)
        val GREEN: FullColor get() = fromRgb(0, 128, 0)
        val BLUE: FullColor get() = fromRgb(0, 0, 255)
        val YELLOW: FullColor get() = fromRgb(255, 255, 0)
        val CYAN: FullColor get() = fromRgb(0, 255, 255)
        val MAGENTA: FullColor get() = fromRgb(255, 0, 255)
        val TRANSPARENT: FullColor get() = fromRgba(0, 0, 0, 0)
    }

    // ── Output conversions ─────────────────────────────────────────────────────

    /** Returns the OKLab representation of this color. */
    fun toOkLab(): OkLab = lab

    /** Returns the OKLch representation of this color. */
    fun toOkLch(): OkLch = lab.toOkLch()

    /**
     * Returns sRGB components as a triple of [0, 255] integers: (red, green, blue).
     * Components are clamped to the displayable range.
     */
    fun toRgb(): Triple<Int, Int, Int> {
        val (rl, gl, bl) = OkLab.toLinearRgb(lab)
        return Triple(
            (linearToSrgb(rl.clamp01()) * 255f + 0.5f).toInt().coerceIn(0, 255),
            (linearToSrgb(gl.clamp01()) * 255f + 0.5f).toInt().coerceIn(0, 255),
            (linearToSrgb(bl.clamp01()) * 255f + 0.5f).toInt().coerceIn(0, 255),
        )
    }

    /**
     * Returns sRGBA components as a [RgbaComponents] object with integer channels (0–255).
     */
    fun toRgba(): RgbaComponents {
        val (r, g, b) = toRgb()
        return RgbaComponents(r, g, b, (alpha * 255f + 0.5f).toInt().coerceIn(0, 255))
    }

    /**
     * Returns floating-point sRGB components in [0, 1].
     */
    fun toRgbFloat(): Triple<Float, Float, Float> {
        val (rl, gl, bl) = OkLab.toLinearRgb(lab)
        return Triple(
            linearToSrgb(rl.clamp01()).clamp01(),
            linearToSrgb(gl.clamp01()).clamp01(),
            linearToSrgb(bl.clamp01()).clamp01(),
        )
    }

    /**
     * Returns a CSS hex string in the form `#RRGGBB` (or `#RRGGBBAA` if [includeAlpha] is true
     * and alpha is not fully opaque).
     */
    fun toHex(includeAlpha: Boolean = false): String {
        val (r, g, b) = toRgb()
        val a = (alpha * 255f + 0.5f).toInt().coerceIn(0, 255)
        return if (includeAlpha && a != 255) {
            "#%02X%02X%02X%02X".format(r, g, b, a)
        } else {
            "#%02X%02X%02X".format(r, g, b)
        }
    }

    /**
     * Returns an Android-compatible packed ARGB integer.
     * Equivalent to `android.graphics.Color.argb(a, r, g, b)`.
     */
    fun toAndroidArgb(): Int {
        val (r, g, b) = toRgb()
        val a = (alpha * 255f + 0.5f).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    /**
     * Returns HSL components as a [HslComponents] object.
     * Hue is in [0, 360), saturation and lightness in [0, 1].
     */
    fun toHsl(): HslComponents {
        val (r, g, b) = toRgbFloat()
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val delta = max - min

        if (delta < 1e-6f) {
            return HslComponents(0f, 0f, l)
        }

        val s = delta / (1f - abs(2f * l - 1f))
        val h = when (max) {
            r -> (((g - b) / delta) % 6f + 6f) % 6f * 60f
            g -> (((b - r) / delta) + 2f) * 60f
            else -> (((r - g) / delta) + 4f) * 60f
        }
        return HslComponents(h, s.clamp01(), l.clamp01())
    }

    /**
     * Returns HSV components as an [HsvComponents] object.
     * Hue is in [0, 360), saturation and value in [0, 1].
     */
    fun toHsv(): HsvComponents {
        val (r, g, b) = toRgbFloat()
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val h = if (delta < 1e-6f) {
            0f
        } else when (max) {
            r -> (((g - b) / delta) % 6f + 6f) % 6f * 60f
            g -> (((b - r) / delta) + 2f) * 60f
            else -> (((r - g) / delta) + 4f) * 60f
        }
        val s = if (max < 1e-6f) 0f else (delta / max).clamp01()
        return HsvComponents(h, s, max.clamp01())
    }

    // ── Manipulation ───────────────────────────────────────────────────────────

    // ── Axis readers ──────────────────────────────────────────────────────────

    /** The OKLab lightness of this color in [0, 1]. */
    val lightness: Float get() = lab.L

    /** The OKLch chroma of this color (≥ 0). */
    val chroma: Float get() = lab.toOkLch().C

    /** The OKLch hue of this color in [0, 360). */
    val hue: Float get() = lab.toOkLch().H

    // ── Internal axis-mutation helpers ────────────────────────────────────────

    /** Return a new color by transforming the internal [OkLab] value; alpha is preserved. */
    private fun modifyLab(transform: (OkLab) -> OkLab): FullColor = FullColor(transform(lab), alpha)

    /** Return a new color by transforming the [OkLch] representation; alpha is preserved. */
    private fun modifyLch(transform: (OkLch) -> OkLch): FullColor =
        FullColor(transform(lab.toOkLch()).toOkLab(), alpha)

    // ── Lightness ─────────────────────────────────────────────────────────────

    /**
     * Lighten the color by [amount] (0–1) in OKLab lightness.
     *
     * - `amount = 0` → color is unchanged.
     * - `amount = 1` → shortcut to pure [WHITE] (exact `#ffffff`, alpha preserved).
     * - Values in between shift OKLab `L` upward by [amount] and clamp to `[0, 1]`.
     */
    fun lighten(amount: Float): FullColor {
        val clamped = amount.coerceIn(0f, 1f)
        if (clamped >= 1f) return WHITE.withAlpha(alpha)
        val newL = (lab.L + clamped).coerceIn(0f, 1f)
        return FullColor(OkLab(newL, lab.a, lab.b), alpha)
    }

    /**
     * Darken the color by [amount] (0–1) in OKLab lightness.
     *
     * - `amount = 0` → color is unchanged.
     * - `amount = 1` → shortcut to pure [BLACK] (exact `#000000`, alpha preserved).
     * - Values in between shift OKLab `L` downward by [amount] and clamp to `[0, 1]`.
     */
    fun darken(amount: Float): FullColor {
        val clamped = amount.coerceIn(0f, 1f)
        if (clamped >= 1f) return BLACK.withAlpha(alpha)
        val newL = (lab.L - clamped).coerceIn(0f, 1f)
        return FullColor(OkLab(newL, lab.a, lab.b), alpha)
    }

    /**
     * Shift the OKLab lightness by [amount] (positive = lighter, negative = darker).
     * The result is clamped to [0, 1].
     *
     * Unlike [lighten] / [darken] (which accept only positive magnitudes), this
     * function accepts a signed offset, making it convenient for symmetrical
     * increase/decrease operations.
     */
    fun adjustLightness(amount: Float): FullColor =
        modifyLab { OkLab((it.L + amount).coerceIn(0f, 1f), it.a, it.b) }

    /**
     * Return a new color with the OKLab lightness set to [lightness] (0–1, clamped).
     * The a/b chroma axes are preserved.
     */
    fun withLightness(lightness: Float): FullColor =
        modifyLab { OkLab(lightness.coerceIn(0f, 1f), it.a, it.b) }

    // ── Chroma ────────────────────────────────────────────────────────────────

    /**
     * Saturate the color by [amount] (0–1) — scales the OKLch chroma upward.
     */
    fun saturate(amount: Float): FullColor =
        modifyLch { OkLch(it.L, (it.C * (1f + amount.coerceIn(0f, 1f))).coerceAtLeast(0f), it.H) }

    /**
     * Desaturate the color by [amount] (0–1) — scales the OKLch chroma downward.
     * An [amount] of 1 produces a grey of the same lightness.
     */
    fun desaturate(amount: Float): FullColor =
        modifyLch { OkLch(it.L, (it.C * (1f - amount.coerceIn(0f, 1f))).coerceAtLeast(0f), it.H) }

    /**
     * Shift the OKLch chroma by [amount] (positive = more vivid, negative = less vivid).
     * The result is clamped to ≥ 0.
     */
    fun adjustChroma(amount: Float): FullColor =
        modifyLch { OkLch(it.L, (it.C + amount).coerceAtLeast(0f), it.H) }

    /**
     * Return a new color with the OKLch chroma set to [chroma] (≥ 0, clamped).
     * Lightness and hue are preserved.
     */
    fun withChroma(chroma: Float): FullColor =
        modifyLch { OkLch(it.L, chroma.coerceAtLeast(0f), it.H) }

    // ── Hue ───────────────────────────────────────────────────────────────────

    /**
     * Shift the OKLch hue by [degrees] (positive or negative).
     * The result wraps around [0, 360).
     */
    fun adjustHue(degrees: Float): FullColor =
        modifyLch { OkLch(it.L, it.C, (it.H + degrees + 360f) % 360f) }

    /**
     * Return a new color with the OKLch hue set to [degrees] (0–360°, normalized).
     * Lightness and chroma are preserved.
     */
    fun withHue(degrees: Float): FullColor =
        modifyLch { OkLch(it.L, it.C, ((degrees % 360f) + 360f) % 360f) }

    // ── Harmony ───────────────────────────────────────────────────────────────

    /**
     * Return the complementary color (hue rotated 180°).
     */
    fun complement(): FullColor = adjustHue(180f)

    /**
     * Return a split-complementary pair: two colors each [angle] degrees away from
     * the complementary hue. Defaults to a 30° split.
     */
    fun splitComplementary(angle: Float = 30f): Pair<FullColor, FullColor> =
        adjustHue(180f - angle) to adjustHue(180f + angle)

    /**
     * Return a triadic color scheme: three colors evenly spaced at 120° intervals.
     * This color is the first element of the triple.
     */
    fun triadic(): Triple<FullColor, FullColor, FullColor> =
        Triple(this, adjustHue(120f), adjustHue(240f))

    /**
     * Return an analogous color scheme: three colors separated by [angle] degrees
     * (default 30°). This color is the center element of the triple.
     */
    fun analogous(angle: Float = 30f): Triple<FullColor, FullColor, FullColor> =
        Triple(adjustHue(-angle), this, adjustHue(angle))

    // ── Blending ──────────────────────────────────────────────────────────────

    /**
     * Mix this color with [other] at the given [ratio] (0 = this, 1 = other)
     * using linear interpolation in OKLab space.
     */
    fun mix(other: FullColor, ratio: Float = 0.5f): FullColor {
        val t = ratio.coerceIn(0f, 1f)
        val mixedAlpha = alpha + (other.alpha - alpha) * t
        return FullColor(lab.lerp(other.lab, t), mixedAlpha)
    }

    /**
     * Return a new color with the alpha channel set to [newAlpha] (0–1).
     */
    fun withAlpha(newAlpha: Float): FullColor = FullColor(lab, newAlpha.coerceIn(0f, 1f))

    // ── Accessibility ─────────────────────────────────────────────────────────

    /**
     * Compute the relative luminance (WCAG) of this color in the range [0, 1].
     * Useful for accessibility contrast calculations.
     */
    fun relativeLuminance(): Float {
        val (rl, gl, bl) = OkLab.toLinearRgb(lab)
        return (0.2126f * rl.clamp01() + 0.7152f * gl.clamp01() + 0.0722f * bl.clamp01())
            .clamp01()
    }

    /**
     * Compute the WCAG contrast ratio between this color and [other] (1:1 to 21:1).
     */
    fun contrastRatio(other: FullColor): Float {
        val l1 = relativeLuminance()
        val l2 = other.relativeLuminance()
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Return whichever of [light] (default: [WHITE]) or [dark] (default: [BLACK])
     * has higher WCAG contrast against this color.
     *
     * Useful for choosing a foreground text color that will be legible on this
     * background.
     */
    fun onColor(light: FullColor = WHITE, dark: FullColor = BLACK): FullColor {
        val lightRatio = contrastRatio(light)
        val darkRatio = contrastRatio(dark)
        return if (lightRatio >= darkRatio) light else dark
    }

    /**
     * Return this color, or a lightness-adjusted version, that achieves at least
     * [minRatio] WCAG contrast against [background] (default WCAG AA: 4.5:1).
     *
     * The function shifts lightness toward the direction that can achieve the
     * higher maximum contrast. If the target cannot be reached the best
     * achievable result is returned.
     */
    fun ensureContrast(background: FullColor, minRatio: Float = 4.5f): FullColor {
        if (contrastRatio(background) >= minRatio) return this
        val bgLum = background.relativeLuminance()
        // Choose the direction that can achieve the highest possible contrast.
        // Going lighter tops out at 1.05/(bgLum+0.05); going darker at (bgLum+0.05)/0.05.
        val maxIfLighter = 1.05f / (bgLum + 0.05f)
        val maxIfDarker = (bgLum + 0.05f) / 0.05f
        val goLighter = maxIfLighter >= maxIfDarker
        var candidate = this
        val step = 0.05f
        repeat(20) {
            val adjusted = if (goLighter) candidate.lighten(step) else candidate.darken(step)
            candidate = if (adjusted == candidate) {
                // L is clamped at the boundary; jump directly to pure white/black.
                val boundary = if (goLighter) candidate.lighten(1f) else candidate.darken(1f)
                if (boundary == candidate) return candidate
                boundary
            } else {
                adjusted
            }
            if (candidate.contrastRatio(background) >= minRatio) return candidate
        }
        return candidate
    }

    // ── Object overrides ───────────────────────────────────────────────────────

    override fun toString(): String = toHex(includeAlpha = alpha < 1f)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullColor) return false
        return lab == other.lab && alpha == other.alpha
    }

    override fun hashCode(): Int = 31 * lab.hashCode() + alpha.hashCode()
}

/** Container for sRGBA integer components (0–255 each). */
data class RgbaComponents(val r: Int, val g: Int, val b: Int, val a: Int)

/** Container for HSL components. Hue in [0, 360), saturation and lightness in [0, 1]. */
data class HslComponents(val h: Float, val s: Float, val l: Float)

/** Container for HSV components. Hue in [0, 360), saturation and value in [0, 1]. */
data class HsvComponents(val h: Float, val s: Float, val v: Float)

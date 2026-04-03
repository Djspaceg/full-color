package com.resourcefork.fullcolor

/**
 * Additional color manipulation extensions for [FullColor].
 *
 * These functions complement the core manipulation API on [FullColor] with absolute
 * OKLch setters, relative adjusters, accessibility helpers, and color-harmony
 * shortcuts. They form the pure-Kotlin implementation layer that
 * `full-color-compose`'s `ComposeColorExtensions` wraps as extension functions on
 * `androidx.compose.ui.graphics.Color`.
 */

// ── Absolute setters in OKLch ──────────────────────────────────────────────────

/**
 * Return a new color with the OKLch hue set to [degrees] (0–360).
 * Lightness and chroma are preserved.
 */
fun FullColor.setHue(degrees: Float): FullColor {
    val lch = toOkLch()
    return FullColor.fromOkLch(OkLch(lch.L, lch.C, ((degrees % 360f) + 360f) % 360f), alpha)
}

/**
 * Return a new color with the OKLab lightness set to [lightness] (0–1, clamped).
 * The a/b chroma axes are preserved.
 */
fun FullColor.setLightness(lightness: Float): FullColor {
    val lab = toOkLab()
    return FullColor.fromOkLab(OkLab(lightness.coerceIn(0f, 1f), lab.a, lab.b), alpha)
}

/**
 * Return a new color with the OKLch chroma set to [chroma] (≥ 0, clamped).
 * Lightness and hue are preserved.
 */
fun FullColor.setChroma(chroma: Float): FullColor {
    val lch = toOkLch()
    return FullColor.fromOkLch(OkLch(lch.L, chroma.coerceAtLeast(0f), lch.H), alpha)
}

// ── Relative adjusters ─────────────────────────────────────────────────────────

/**
 * Shift the OKLab lightness by [amount] (positive = lighter, negative = darker).
 * The result is clamped to [0, 1].
 *
 * Unlike [FullColor.lighten] / [FullColor.darken] (which accept only positive
 * magnitudes), this function accepts a signed offset, making it convenient for
 * symmetrical increase/decrease operations.
 */
fun FullColor.adjustLightness(amount: Float): FullColor {
    val lab = toOkLab()
    return FullColor.fromOkLab(OkLab((lab.L + amount).coerceIn(0f, 1f), lab.a, lab.b), alpha)
}

/**
 * Scale the OKLch chroma by `(1 + amount)`.
 *
 * Positive values increase saturation; negative values decrease it.
 * An [amount] of `-1` fully desaturates; values above `0` boost vividness.
 */
fun FullColor.adjustSaturation(amount: Float): FullColor {
    val lch = toOkLch()
    val newC = (lch.C * (1f + amount)).coerceAtLeast(0f)
    return FullColor.fromOkLch(OkLch(lch.L, newC, lch.H), alpha)
}

/**
 * Shift the OKLch chroma by [amount] (positive = more vivid, negative = less vivid).
 * The result is clamped to ≥ 0.
 */
fun FullColor.adjustChroma(amount: Float): FullColor {
    val lch = toOkLch()
    val newC = (lch.C + amount).coerceAtLeast(0f)
    return FullColor.fromOkLch(OkLch(lch.L, newC, lch.H), alpha)
}

// ── Accessibility helpers ──────────────────────────────────────────────────────

/**
 * Return whichever of [light] (default: [FullColor.WHITE]) or [dark]
 * (default: [FullColor.BLACK]) has higher WCAG contrast against this color.
 *
 * Useful for choosing a foreground text color that will be legible on this
 * background color.
 */
fun FullColor.onColor(
    light: FullColor = FullColor.WHITE,
    dark: FullColor = FullColor.BLACK,
): FullColor = ColorUtils.bestContrast(this, light, dark)

/**
 * Return this color, or a lightness-adjusted version of it, that achieves at
 * least [minRatio] WCAG contrast against [background] (default WCAG AA: 4.5:1).
 *
 * The function iteratively shifts the lightness of this color away from the
 * background's luminance in steps of 5% OKLab L until the ratio is met or a
 * limit is reached. If the target ratio cannot be achieved the closest result
 * is returned.
 */
fun FullColor.ensureContrast(background: FullColor, minRatio: Float = 4.5f): FullColor {
    if (contrastRatio(background) >= minRatio) return this
    val bgLum = background.relativeLuminance()
    // Choose the direction that can achieve the highest possible contrast.
    // Going lighter tops out at 1.05/(bgLum+0.05); going darker tops out at (bgLum+0.05)/0.05.
    val maxIfLighter = 1.05f / (bgLum + 0.05f)
    val maxIfDarker = (bgLum + 0.05f) / 0.05f
    val goLighter = maxIfLighter >= maxIfDarker
    var candidate = this
    val step = 0.05f
    repeat(20) {
        val adjusted = if (goLighter) candidate.lighten(step) else candidate.darken(step)
        candidate = if (adjusted == candidate) {
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

// ── Color-harmony shortcuts ────────────────────────────────────────────────────

/**
 * Return the complementary color (hue rotated 180°).
 *
 * Alias for [FullColor.complement] using the name expected by
 * `ComposeColorExtensions`.
 */
fun FullColor.complementary(): FullColor = complement()

/**
 * Return a split-complementary pair: two colors each [angle] degrees away from
 * the complementary hue (default 30°).
 */
fun FullColor.splitComplementary(angle: Float = 30f): Pair<FullColor, FullColor> =
    ColorUtils.splitComplement(this, angle)

/**
 * Return a triadic color scheme: three colors evenly spaced at 120° intervals.
 * This color is the first element of the triple.
 */
fun FullColor.triadic(): Triple<FullColor, FullColor, FullColor> =
    ColorUtils.triadic(this)

/**
 * Return an analogous color scheme: three colors separated by [angle] degrees
 * (default 30°). This color is the center element of the triple.
 */
fun FullColor.analogous(angle: Float = 30f): Triple<FullColor, FullColor, FullColor> =
    ColorUtils.analogous(this, angle)

/**
 * Mix this color with [other] at the given [ratio] (0 = this, 1 = other) in
 * OKLab space.
 *
 * Alias for [FullColor.mix] using the name expected by `ComposeColorExtensions`.
 */
fun FullColor.mixWith(other: FullColor, ratio: Float = 0.5f): FullColor = mix(other, ratio)

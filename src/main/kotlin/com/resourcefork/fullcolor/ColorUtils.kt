package com.resourcefork.fullcolor

/**
 * High-level color harmony and palette utilities.
 *
 * All rotations and manipulations are performed in OKLch space to preserve
 * perceptual uniformity.
 */
object ColorUtils {

    /**
     * Return the complementary color (180° hue rotation).
     */
    fun complement(color: FullColor): FullColor = color.complement()

    /**
     * Return a split-complementary pair: two colors each [angle] degrees away from
     * the complementary hue. Defaults to a 30° split.
     */
    fun splitComplement(color: FullColor, angle: Float = 30f): Pair<FullColor, FullColor> =
        color.rotateHue(180f - angle) to color.rotateHue(180f + angle)

    /**
     * Return an analogous color scheme: three colors separated by [angle] degrees.
     * The center of the trio is [color] itself.
     */
    fun analogous(color: FullColor, angle: Float = 30f): Triple<FullColor, FullColor, FullColor> =
        Triple(color.rotateHue(-angle), color, color.rotateHue(angle))

    /**
     * Return a triadic color scheme: three colors evenly spaced at 120° intervals.
     */
    fun triadic(color: FullColor): Triple<FullColor, FullColor, FullColor> =
        Triple(color, color.rotateHue(120f), color.rotateHue(240f))

    /**
     * Return a tetradic (square) color scheme: four colors spaced at 90° intervals.
     */
    fun tetradic(color: FullColor): List<FullColor> =
        listOf(color, color.rotateHue(90f), color.rotateHue(180f), color.rotateHue(270f))

    /**
     * Determine whether [foreground] has sufficient WCAG AA contrast against [background].
     * Normal text requires a contrast ratio of at least 4.5:1.
     */
    fun isWcagAaCompliant(foreground: FullColor, background: FullColor): Boolean =
        foreground.contrastRatio(background) >= 4.5f

    /**
     * Determine whether [foreground] has sufficient WCAG AAA contrast against [background].
     * Normal text requires a contrast ratio of at least 7:1.
     */
    fun isWcagAaaCompliant(foreground: FullColor, background: FullColor): Boolean =
        foreground.contrastRatio(background) >= 7f

    /**
     * Choose the better-contrasting color (either [light] or [dark]) to place on top
     * of [background]. Defaults to white for [light] and black for [dark].
     */
    fun bestContrast(
        background: FullColor,
        light: FullColor = FullColor.WHITE,
        dark: FullColor = FullColor.BLACK,
    ): FullColor {
        val lightRatio = background.contrastRatio(light)
        val darkRatio = background.contrastRatio(dark)
        return if (lightRatio >= darkRatio) light else dark
    }

    /**
     * Generate a monochromatic palette of [steps] lightness steps for [color],
     * spanning from a darker to a lighter variant in OKLab space.
     */
    fun monochromaticPalette(color: FullColor, steps: Int = 5): List<FullColor> {
        require(steps >= 2) { "steps must be at least 2" }
        val darkVariant = color.darken(0.4f)
        val lightVariant = color.lighten(0.4f)
        return ColorRamp.generate(darkVariant, lightVariant, steps)
    }

    /**
     * Parse a CSS color string and return a [FullColor].
     *
     * Supported formats:
     * - Hex: `#RGB`, `#RGBA`, `#RRGGBB`, `#RRGGBBAA`
     * - `rgb(r, g, b)` or `rgba(r, g, b, a)` — integer 0–255 (alpha 0–1 for rgba)
     * - `hsl(h, s%, l%)` or `hsla(h, s%, l%, a)` — as per CSS
     * - Named colors: `black`, `white`, `red`, `green`, `blue`, `yellow`, `cyan`, `magenta`, `transparent`
     */
    fun parseCss(css: String): FullColor {
        val s = css.trim().lowercase()
        return when {
            s.startsWith('#') -> FullColor.fromHex(s)
            s.startsWith("rgba(") -> parseCssRgba(s)
            s.startsWith("rgb(") -> parseCssRgb(s)
            s.startsWith("hsla(") -> parseCssHsla(s)
            s.startsWith("hsl(") -> parseCssHsl(s)
            else -> parseNamedColor(s)
                ?: throw IllegalArgumentException("Unsupported CSS color: $css")
        }
    }

    private fun parseCssRgb(s: String): FullColor {
        val nums = extractNumbers(s)
        require(nums.size >= 3) { "Expected 3 values in $s" }
        return FullColor.fromRgb(nums[0].toInt(), nums[1].toInt(), nums[2].toInt())
    }

    private fun parseCssRgba(s: String): FullColor {
        val nums = extractNumbers(s)
        require(nums.size >= 4) { "Expected 4 values in $s" }
        val a = (nums[3] * 255f + 0.5f).toInt().coerceIn(0, 255)
        return FullColor.fromRgba(nums[0].toInt(), nums[1].toInt(), nums[2].toInt(), a)
    }

    private fun parseCssHsl(s: String): FullColor {
        val nums = extractNumbers(s)
        require(nums.size >= 3) { "Expected 3 values in $s" }
        return FullColor.fromHsl(nums[0], nums[1] / 100f, nums[2] / 100f)
    }

    private fun parseCssHsla(s: String): FullColor {
        val nums = extractNumbers(s)
        require(nums.size >= 4) { "Expected 4 values in $s" }
        return FullColor.fromHsl(nums[0], nums[1] / 100f, nums[2] / 100f, nums[3])
    }

    private fun extractNumbers(s: String): List<Float> {
        val content = s.substringAfter('(').substringBefore(')')
        return content.split(',').map { it.trim().trimEnd('%').toFloat() }
    }

    private fun parseNamedColor(name: String): FullColor? = when (name) {
        "black" -> FullColor.BLACK
        "white" -> FullColor.WHITE
        "red" -> FullColor.RED
        "green" -> FullColor.GREEN
        "blue" -> FullColor.BLUE
        "yellow" -> FullColor.YELLOW
        "cyan" -> FullColor.CYAN
        "magenta" -> FullColor.MAGENTA
        "transparent" -> FullColor.TRANSPARENT
        else -> null
    }
}

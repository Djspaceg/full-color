package com.resourcefork.fullcolor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** Tests for the extension functions in SrgbColorExtensions.kt. */
class SrgbColorExtensionsTest {

    private fun assertNear(expected: Float, actual: Float, delta: Float = 0.02f, message: String = "") {
        assertTrue(abs(actual - expected) <= delta, "$message: expected $expected but got $actual (delta $delta)")
    }

    // ── setHue ────────────────────────────────────────────────────────────────

    @Test
    fun `setHue changes hue and preserves lightness and chroma`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val result = base.setHue(200f)
        val baseLch = base.toOkLch()
        val resultLch = result.toOkLch()
        assertNear(200f, resultLch.H, 3f, "hue should be near 200°")
        assertNear(baseLch.L, resultLch.L, 0.01f, "lightness should be preserved")
        assertNear(baseLch.C, resultLch.C, 0.01f, "chroma should be preserved")
    }

    @Test
    fun `setHue normalises degrees above 360`() {
        val base = FullColor.fromHsl(90f, 0.7f, 0.5f)
        val result = base.setHue(400f) // 400 % 360 == 40
        assertNear(40f, result.toOkLch().H, 3f, "setHue(400) should produce ~40° hue")
    }

    @Test
    fun `setHue normalises negative degrees`() {
        val base = FullColor.fromHsl(90f, 0.7f, 0.5f)
        val result = base.setHue(-20f) // equivalent to 340°
        assertNear(340f, result.toOkLch().H, 3f, "setHue(-20) should produce ~340° hue")
    }

    // ── setLightness ──────────────────────────────────────────────────────────

    @Test
    fun `setLightness changes OKLab L to specified value`() {
        val base = FullColor.fromRgb(200, 100, 50)
        val result = base.setLightness(0.8f)
        assertNear(0.8f, result.toOkLab().L, 0.01f, "OKLab L should be 0.8")
    }

    @Test
    fun `setLightness clamps to 0`() {
        val base = FullColor.fromRgb(100, 100, 100)
        val result = base.setLightness(-0.5f)
        assertNear(0f, result.toOkLab().L, 0.001f, "setLightness should clamp to 0")
    }

    @Test
    fun `setLightness clamps to 1`() {
        val base = FullColor.fromRgb(100, 100, 100)
        val result = base.setLightness(1.5f)
        assertNear(1f, result.toOkLab().L, 0.001f, "setLightness should clamp to 1")
    }

    // ── setChroma ─────────────────────────────────────────────────────────────

    @Test
    fun `setChroma changes OKLch chroma to specified value`() {
        val base = FullColor.fromHsl(120f, 0.9f, 0.5f)
        val target = 0.05f
        val result = base.setChroma(target)
        assertNear(target, result.toOkLch().C, 0.005f, "OKLch C should be near $target")
    }

    @Test
    fun `setChroma clamps to 0`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val result = base.setChroma(-1f)
        assertNear(0f, result.toOkLch().C, 0.001f, "setChroma should clamp to 0")
    }

    @Test
    fun `setChroma zero produces grey`() {
        val base = FullColor.fromHsl(90f, 0.8f, 0.5f)
        val grey = base.setChroma(0f)
        assertNear(0f, grey.toOkLch().C, 0.01f, "C=0 should produce achromatic color")
    }

    // ── adjustLightness ───────────────────────────────────────────────────────

    @Test
    fun `adjustLightness positive shifts L up`() {
        val base = FullColor.fromRgb(100, 100, 100)
        val baseL = base.toOkLab().L
        val result = base.adjustLightness(0.2f)
        assertNear(baseL + 0.2f, result.toOkLab().L, 0.01f, "L should increase by 0.2")
    }

    @Test
    fun `adjustLightness negative shifts L down`() {
        val base = FullColor.fromRgb(200, 200, 200)
        val baseL = base.toOkLab().L
        val result = base.adjustLightness(-0.2f)
        assertNear(baseL - 0.2f, result.toOkLab().L, 0.01f, "L should decrease by 0.2")
    }

    @Test
    fun `adjustLightness clamps at 0`() {
        val result = FullColor.fromRgb(10, 10, 10).adjustLightness(-1f)
        assertNear(0f, result.toOkLab().L, 0.01f)
    }

    @Test
    fun `adjustLightness clamps at 1`() {
        val result = FullColor.fromRgb(200, 200, 200).adjustLightness(1f)
        assertNear(1f, result.toOkLab().L, 0.01f)
    }

    // ── adjustSaturation ──────────────────────────────────────────────────────

    @Test
    fun `adjustSaturation positive increases chroma`() {
        val base = FullColor.fromHsl(60f, 0.5f, 0.5f)
        val result = base.adjustSaturation(0.5f)
        assertTrue(result.toOkLch().C > base.toOkLch().C, "chroma should increase")
    }

    @Test
    fun `adjustSaturation minus 1 fully desaturates`() {
        val base = FullColor.fromHsl(60f, 0.9f, 0.5f)
        val result = base.adjustSaturation(-1f)
        assertNear(0f, result.toOkLch().C, 0.01f, "C should be near zero after -1 saturation")
    }

    @Test
    fun `adjustSaturation zero leaves chroma unchanged`() {
        val base = FullColor.fromHsl(200f, 0.7f, 0.4f)
        val result = base.adjustSaturation(0f)
        assertNear(base.toOkLch().C, result.toOkLch().C, 0.001f)
    }

    // ── adjustChroma ──────────────────────────────────────────────────────────

    @Test
    fun `adjustChroma positive increases chroma`() {
        val base = FullColor.fromHsl(180f, 0.5f, 0.5f)
        val baseC = base.toOkLch().C
        val result = base.adjustChroma(0.05f)
        assertNear(baseC + 0.05f, result.toOkLch().C, 0.005f, "chroma should shift up by 0.05")
    }

    @Test
    fun `adjustChroma negative decreases chroma`() {
        val base = FullColor.fromHsl(180f, 0.8f, 0.5f)
        val result = base.adjustChroma(-0.05f)
        assertTrue(result.toOkLch().C < base.toOkLch().C, "chroma should decrease")
    }

    @Test
    fun `adjustChroma clamps to 0`() {
        val base = FullColor.fromHsl(100f, 0.6f, 0.5f)
        val result = base.adjustChroma(-10f)
        assertNear(0f, result.toOkLch().C, 0.01f)
    }

    // ── onColor ───────────────────────────────────────────────────────────────

    @Test
    fun `onColor returns black for white background`() {
        val on = FullColor.WHITE.onColor()
        val (r, g, b) = on.toRgb()
        assertEquals(0, r, "onColor(white) should be black")
        assertEquals(0, g)
        assertEquals(0, b)
    }

    @Test
    fun `onColor returns white for black background`() {
        val on = FullColor.BLACK.onColor()
        val (r, g, b) = on.toRgb()
        assertEquals(255, r, "onColor(black) should be white")
        assertEquals(255, g)
        assertEquals(255, b)
    }

    @Test
    fun `onColor returns custom light on dark background`() {
        val customLight = FullColor.fromRgb(200, 200, 200)
        val on = FullColor.BLACK.onColor(light = customLight, dark = FullColor.fromRgb(50, 50, 50))
        assertEquals(customLight.toRgb(), on.toRgb())
    }

    // ── ensureContrast ────────────────────────────────────────────────────────

    @Test
    fun `ensureContrast on passing color returns same instance`() {
        val fg = FullColor.BLACK
        val bg = FullColor.WHITE
        val result = fg.ensureContrast(bg)
        // Black on white already exceeds 4.5:1
        assertTrue(result.contrastRatio(bg) >= 4.5f)
    }

    @Test
    fun `ensureContrast raises contrast of low-contrast pair`() {
        // Mid-grey on mid-grey has ~1:1 contrast
        val fg = FullColor.fromRgb(128, 128, 128)
        val bg = FullColor.fromRgb(128, 128, 128)
        val result = fg.ensureContrast(bg, 4.5f)
        assertTrue(
            result.contrastRatio(bg) >= 4.5f,
            "adjusted color should meet 4.5:1 against matching background",
        )
    }

    @Test
    fun `ensureContrast custom minRatio respected`() {
        val fg = FullColor.fromRgb(150, 100, 50)
        val bg = FullColor.fromRgb(130, 90, 40)
        val result = fg.ensureContrast(bg, 3f)
        assertTrue(
            result.contrastRatio(bg) >= 3f,
            "adjusted color should meet at least 3:1",
        )
    }

    // ── complementary ────────────────────────────────────────────────────────

    @Test
    fun `complementary is 180 degree hue rotation`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val comp = base.complementary()
        val baseH = base.toOkLch().H
        val compH = comp.toOkLch().H
        val d = abs(compH - baseH)
        val angDiff = minOf(d, 360f - d)
        assertNear(180f, angDiff, 3f, "complementary should be ~180° rotated")
    }

    @Test
    fun `complementary matches complement`() {
        val base = FullColor.fromHsl(120f, 0.7f, 0.4f)
        assertEquals(base.complement().toRgb(), base.complementary().toRgb())
    }

    // ── splitComplementary ────────────────────────────────────────────────────

    @Test
    fun `splitComplementary returns two colors`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val (left, right) = base.splitComplementary()
        assertNotEquals(base.toRgb(), left.toRgb())
        assertNotEquals(base.toRgb(), right.toRgb())
    }

    @Test
    fun `splitComplementary default angle is 30 degrees from complement`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val (left, right) = base.splitComplementary(30f)
        val baseH = base.toOkLch().H
        val leftH = left.toOkLch().H
        val rightH = right.toOkLch().H
        // left should be ~150° from base, right ~210° from base
        val diffLeft = ((leftH - baseH + 360f) % 360f)
        val diffRight = ((rightH - baseH + 360f) % 360f)
        assertNear(150f, diffLeft, 5f, "left split should be ~150° from base")
        assertNear(210f, diffRight, 5f, "right split should be ~210° from base")
    }

    // ── triadic ───────────────────────────────────────────────────────────────

    @Test
    fun `triadic returns 3 colors 120 degrees apart`() {
        val base = FullColor.fromHsl(0f, 0.8f, 0.5f)
        val (a, b, c) = base.triadic()
        val hA = a.toOkLch().H
        val hB = b.toOkLch().H
        val hC = c.toOkLch().H
        assertNear(120f, (hB - hA + 360f) % 360f, 5f, "A→B gap should be ~120°")
        assertNear(120f, (hC - hB + 360f) % 360f, 5f, "B→C gap should be ~120°")
    }

    @Test
    fun `triadic first element equals base color`() {
        val base = FullColor.fromHsl(45f, 0.9f, 0.4f)
        val (first, _, _) = base.triadic()
        assertEquals(base.toRgb(), first.toRgb())
    }

    // ── analogous ─────────────────────────────────────────────────────────────

    @Test
    fun `analogous center element equals base color`() {
        val base = FullColor.fromHsl(90f, 0.7f, 0.5f)
        val (_, mid, _) = base.analogous()
        assertNear(base.toOkLch().H, mid.toOkLch().H, 2f, "center should match base hue")
    }

    @Test
    fun `analogous flanks are angle degrees away from center`() {
        val base = FullColor.fromHsl(180f, 0.8f, 0.5f)
        val (left, mid, right) = base.analogous(30f)
        val midH = mid.toOkLch().H
        val leftH = left.toOkLch().H
        val rightH = right.toOkLch().H
        assertNear(30f, (midH - leftH + 360f) % 360f, 3f, "left flank should be 30° behind")
        assertNear(30f, (rightH - midH + 360f) % 360f, 3f, "right flank should be 30° ahead")
    }

    // ── mixWith ───────────────────────────────────────────────────────────────

    @Test
    fun `mixWith at 0 returns this color`() {
        val a = FullColor.fromRgb(255, 0, 0)
        val b = FullColor.fromRgb(0, 0, 255)
        assertEquals(a.toRgb(), a.mixWith(b, 0f).toRgb())
    }

    @Test
    fun `mixWith at 1 returns other color`() {
        val a = FullColor.fromRgb(255, 0, 0)
        val b = FullColor.fromRgb(0, 0, 255)
        assertEquals(b.toRgb(), a.mixWith(b, 1f).toRgb())
    }

    @Test
    fun `mixWith matches mix`() {
        val a = FullColor.fromRgb(200, 100, 50)
        val b = FullColor.fromRgb(50, 200, 150)
        val viaExtension = a.mixWith(b, 0.3f)
        val viaMethod = a.mix(b, 0.3f)
        assertEquals(viaMethod.toRgb(), viaExtension.toRgb())
    }
}

package com.resourcefork.fullcolor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** Tests for ColorRamp, ColorGradient, and ColorUtils palette helpers. */
class ColorRampAndUtilsTest {

    private fun assertNear(expected: Float, actual: Float, delta: Float = 0.01f, message: String = "") {
        assertTrue(abs(actual - expected) <= delta, "$message: expected $expected but got $actual (delta $delta)")
    }

    // ── ColorRamp ─────────────────────────────────────────────────────────────

    @Test
    fun `ColorRamp generate has correct step count`() {
        val ramp = ColorRamp.generate(FullColor.BLACK, FullColor.WHITE, 5)
        assertEquals(5, ramp.size)
    }

    @Test
    fun `ColorRamp first and last colors match start and end`() {
        val start = FullColor.fromRgb(255, 0, 0)
        val end = FullColor.fromRgb(0, 0, 255)
        val ramp = ColorRamp.generate(start, end, 10)
        val (r1, g1, b1) = ramp.first().toRgb()
        val (r2, g2, b2) = ramp.last().toRgb()
        assertEquals(255, r1); assertEquals(0, g1); assertEquals(0, b1)
        assertEquals(0, r2); assertEquals(0, g2); assertEquals(255, b2)
    }

    @Test
    fun `ColorRamp is monotone in OKLab L for black to white`() {
        val ramp = ColorRamp.generate(FullColor.BLACK, FullColor.WHITE, 8)
        val lValues = ramp.map { it.toOkLab().L }
        for (i in 1 until lValues.size) {
            assertTrue(lValues[i] >= lValues[i - 1] - 1e-5f, "L should be non-decreasing at step $i")
        }
    }

    @Test
    fun `ColorRamp generateLch has correct step count`() {
        val ramp = ColorRamp.generateLch(FullColor.RED, FullColor.BLUE, 7)
        assertEquals(7, ramp.size)
    }

    @Test
    fun `ColorRamp throws for steps less than 2`() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorRamp.generate(FullColor.BLACK, FullColor.WHITE, 1)
        }
    }

    // ── ColorGradient ─────────────────────────────────────────────────────────

    @Test
    fun `ColorGradient sample at 0 returns first color`() {
        val g = ColorGradient.of(FullColor.RED, FullColor.GREEN, FullColor.BLUE)
        val sampled = g.sample(0f)
        val (r, gr, b) = sampled.toRgb()
        assertEquals(255, r); assertEquals(0, gr); assertEquals(0, b)
    }

    @Test
    fun `ColorGradient sample at 1 returns last color`() {
        val g = ColorGradient.of(FullColor.RED, FullColor.GREEN, FullColor.BLUE)
        val sampled = g.sample(1f)
        val (r, gr, b) = sampled.toRgb()
        assertEquals(0, r); assertEquals(0, gr); assertEquals(255, b)
    }

    @Test
    fun `ColorGradient sample count returns correct list size`() {
        val g = ColorGradient.of(FullColor.BLACK, FullColor.WHITE)
        assertEquals(10, g.sample(10).size)
    }

    @Test
    fun `ColorGradient throws for fewer than 2 colors`() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorGradient.of(FullColor.RED)
        }
    }

    @Test
    fun `ColorGradient sample count throws for less than 2`() {
        val g = ColorGradient.of(FullColor.BLACK, FullColor.WHITE)
        assertThrows(IllegalArgumentException::class.java) { g.sample(1) }
    }

    // ── ColorUtils harmonies ───────────────────────────────────────────────────

    @Test
    fun `complement returns a different color`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val comp = ColorUtils.complement(base)
        assertFalse(base.toRgb() == comp.toRgb(), "Complement should differ from original")
    }

    @Test
    fun `triadic returns 3 colors`() {
        val (a, b, c) = ColorUtils.triadic(FullColor.RED)
        // Each should be ~120° apart in hue
        val hA = a.toOkLch().H
        val hB = b.toOkLch().H
        val hC = c.toOkLch().H
        val diffAB = ((hB - hA + 360f) % 360f)
        val diffBC = ((hC - hB + 360f) % 360f)
        assertNear(120f, diffAB, 5f, "Triadic A→B gap")
        assertNear(120f, diffBC, 5f, "Triadic B→C gap")
    }

    @Test
    fun `tetradic returns 4 colors`() {
        assertEquals(4, ColorUtils.tetradic(FullColor.RED).size)
    }

    @Test
    fun `analogous returns 3 colors with base in middle`() {
        val base = FullColor.fromHsl(90f, 0.8f, 0.5f)
        val (left, mid, right) = ColorUtils.analogous(base)
        // Middle should equal base
        assertNear(base.toOkLch().H, mid.toOkLch().H, 2f)
        val leftH = left.toOkLch().H
        val rightH = right.toOkLch().H
        val baseH = base.toOkLch().H
        assertNear(30f, ((baseH - leftH + 360f) % 360f), 3f)
        assertNear(30f, ((rightH - baseH + 360f) % 360f), 3f)
    }

    @Test
    fun `black on white is WCAG AA compliant`() {
        assertTrue(ColorUtils.isWcagAaCompliant(FullColor.BLACK, FullColor.WHITE))
    }

    @Test
    fun `white on white fails WCAG AA`() {
        assertFalse(ColorUtils.isWcagAaCompliant(FullColor.WHITE, FullColor.WHITE))
    }

    @Test
    fun `black on white is WCAG AAA compliant`() {
        assertTrue(ColorUtils.isWcagAaaCompliant(FullColor.BLACK, FullColor.WHITE))
    }

    @Test
    fun `mid grey on white fails WCAG AAA`() {
        assertFalse(ColorUtils.isWcagAaaCompliant(FullColor.fromRgb(120, 120, 120), FullColor.WHITE))
    }

    @Test
    fun `dark grey on white passes WCAG AA large text`() {
        assertTrue(ColorUtils.isWcagAaLargeCompliant(FullColor.fromRgb(90, 90, 90), FullColor.WHITE))
    }

    @Test
    fun `light grey on white fails WCAG AA large text`() {
        assertFalse(ColorUtils.isWcagAaLargeCompliant(FullColor.fromRgb(200, 200, 200), FullColor.WHITE))
    }

    @Test
    fun `bestContrast picks black text on white background`() {
        val best = ColorUtils.bestContrast(FullColor.WHITE)
        val (r, g, b) = best.toRgb()
        assertEquals(0, r); assertEquals(0, g); assertEquals(0, b)
    }

    @Test
    fun `ensureAa meets AA contrast requirement`() {
        val fg = FullColor.fromRgb(128, 128, 128)
        val bg = FullColor.WHITE
        assertTrue(ColorUtils.ensureAa(fg, bg).contrastRatio(bg) >= 4.5f)
    }

    @Test
    fun `ensureAaa meets AAA contrast requirement`() {
        val fg = FullColor.fromRgb(128, 128, 128)
        val bg = FullColor.WHITE
        assertTrue(ColorUtils.ensureAaa(fg, bg).contrastRatio(bg) >= 7.0f)
    }

    @Test
    fun `monochromaticPalette has correct size`() {
        val palette = ColorUtils.monochromaticPalette(FullColor.fromHsl(200f, 0.7f, 0.5f), 7)
        assertEquals(7, palette.size)
    }

    // ── ColorUtils.parseCss ────────────────────────────────────────────────────

    @Test
    fun `parseCss hex string`() {
        val color = ColorUtils.parseCss("#FF0000")
        val (r, g, b) = color.toRgb()
        assertEquals(255, r); assertEquals(0, g); assertEquals(0, b)
    }

    @Test
    fun `parseCss rgb function`() {
        val color = ColorUtils.parseCss("rgb(0, 128, 255)")
        val (r, g, b) = color.toRgb()
        assertEquals(0, r); assertEquals(128, g); assertEquals(255, b)
    }

    @Test
    fun `parseCss rgba function`() {
        val color = ColorUtils.parseCss("rgba(255, 0, 0, 0.5)")
        assertNear(0.5f, color.alpha, 0.02f)
    }

    @Test
    fun `parseCss hsl function`() {
        val color = ColorUtils.parseCss("hsl(0, 100%, 50%)")
        val (r, g, b) = color.toRgb()
        assertEquals(255, r); assertEquals(0, g); assertEquals(0, b)
    }

    @Test
    fun `parseCss named colors`() {
        assertEquals(FullColor.fromRgb(0, 0, 0).toRgb(), ColorUtils.parseCss("black").toRgb())
        assertEquals(FullColor.fromRgb(255, 255, 255).toRgb(), ColorUtils.parseCss("white").toRgb())
    }

    @Test
    fun `parseCss unknown throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            ColorUtils.parseCss("notacolor")
        }
    }
}

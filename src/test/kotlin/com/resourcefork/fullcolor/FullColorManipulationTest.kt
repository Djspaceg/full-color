package com.resourcefork.fullcolor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** Tests for FullColor manipulation methods. */
class FullColorManipulationTest {

    private fun assertNear(expected: Float, actual: Float, delta: Float = 0.01f, message: String = "") {
        assertTrue(abs(actual - expected) <= delta, "$message: expected $expected but got $actual (delta $delta)")
    }

    // ── lighten / darken ──────────────────────────────────────────────────────

    @Test
    fun `lighten increases OKLab L`() {
        val base = FullColor.fromRgb(100, 100, 100)
        val lighter = base.lighten(0.2f)
        assertTrue(lighter.toOkLab().L > base.toOkLab().L, "lightened color should have higher L")
    }

    @Test
    fun `darken decreases OKLab L`() {
        val base = FullColor.fromRgb(150, 150, 150)
        val darker = base.darken(0.2f)
        assertTrue(darker.toOkLab().L < base.toOkLab().L, "darkened color should have lower L")
    }

    @Test
    fun `lighten by 1 produces pure white`() {
        val lightened = FullColor.fromRgb(50, 100, 150).lighten(1f)
        val (r, g, b) = lightened.toRgb()
        assertEquals(255, r, "lighten(1) red should be 255")
        assertEquals(255, g, "lighten(1) green should be 255")
        assertEquals(255, b, "lighten(1) blue should be 255")
    }

    @Test
    fun `darken by 1 produces pure black`() {
        val darkened = FullColor.fromRgb(200, 100, 50).darken(1f)
        val (r, g, b) = darkened.toRgb()
        assertEquals(0, r, "darken(1) red should be 0")
        assertEquals(0, g, "darken(1) green should be 0")
        assertEquals(0, b, "darken(1) blue should be 0")
    }

    // ── adjustLightness / setLightness ────────────────────────────────────────

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
    fun `adjustLightness clamps at boundaries`() {
        assertNear(0f, FullColor.fromRgb(10, 10, 10).adjustLightness(-1f).toOkLab().L, 0.01f)
        assertNear(1f, FullColor.fromRgb(200, 200, 200).adjustLightness(1f).toOkLab().L, 0.01f)
    }

    @Test
    fun `setLightness sets OKLab L to exact value`() {
        val result = FullColor.fromRgb(200, 100, 50).setLightness(0.8f)
        assertNear(0.8f, result.toOkLab().L, 0.01f, "OKLab L should be 0.8")
    }

    @Test
    fun `setLightness clamps out-of-range values`() {
        assertNear(0f, FullColor.fromRgb(100, 100, 100).setLightness(-0.5f).toOkLab().L, 0.001f)
        assertNear(1f, FullColor.fromRgb(100, 100, 100).setLightness(1.5f).toOkLab().L, 0.001f)
    }

    // ── saturate / desaturate ─────────────────────────────────────────────────

    @Test
    fun `desaturate by 1 gives grey`() {
        val grey = FullColor.fromRgb(200, 100, 50).desaturate(1f)
        val lch = grey.toOkLch()
        assertNear(0f, lch.C, 0.01f, "Fully desaturated should have near-zero chroma")
    }

    @Test
    fun `saturate increases chroma`() {
        val base = FullColor.fromRgb(150, 130, 100)
        val saturated = base.saturate(0.5f)
        assertTrue(saturated.toOkLch().C >= base.toOkLch().C)
    }

    // ── adjustSaturation / adjustChroma / setChroma ────────────────────────────

    @Test
    fun `adjustSaturation positive increases chroma`() {
        val base = FullColor.fromHsl(60f, 0.5f, 0.5f)
        assertTrue(base.adjustSaturation(0.5f).toOkLch().C > base.toOkLch().C, "chroma should increase")
    }

    @Test
    fun `adjustSaturation minus 1 fully desaturates`() {
        val result = FullColor.fromHsl(60f, 0.9f, 0.5f).adjustSaturation(-1f)
        assertNear(0f, result.toOkLch().C, 0.01f, "C should be near zero")
    }

    @Test
    fun `adjustSaturation zero leaves chroma unchanged`() {
        val base = FullColor.fromHsl(200f, 0.7f, 0.4f)
        assertNear(base.toOkLch().C, base.adjustSaturation(0f).toOkLch().C, 0.001f)
    }

    @Test
    fun `adjustChroma positive increases chroma`() {
        val base = FullColor.fromHsl(180f, 0.5f, 0.5f)
        val baseC = base.toOkLch().C
        assertNear(baseC + 0.05f, base.adjustChroma(0.05f).toOkLch().C, 0.005f)
    }

    @Test
    fun `adjustChroma negative decreases chroma`() {
        val base = FullColor.fromHsl(180f, 0.8f, 0.5f)
        assertTrue(base.adjustChroma(-0.05f).toOkLch().C < base.toOkLch().C)
    }

    @Test
    fun `adjustChroma clamps to 0`() {
        assertNear(0f, FullColor.fromHsl(100f, 0.6f, 0.5f).adjustChroma(-10f).toOkLch().C, 0.01f)
    }

    @Test
    fun `setChroma sets OKLch C to exact value`() {
        val result = FullColor.fromHsl(120f, 0.9f, 0.5f).setChroma(0.05f)
        assertNear(0.05f, result.toOkLch().C, 0.005f)
    }

    @Test
    fun `setChroma zero produces achromatic color`() {
        assertNear(0f, FullColor.fromHsl(90f, 0.8f, 0.5f).setChroma(0f).toOkLch().C, 0.01f)
    }

    @Test
    fun `setChroma clamps negative to 0`() {
        assertNear(0f, FullColor.fromHsl(60f, 0.8f, 0.5f).setChroma(-1f).toOkLch().C, 0.001f)
    }

    // ── hue rotation ─────────────────────────────────────────────────────────

    @Test
    fun `rotateHue by 0 degrees returns same chroma and lightness`() {
        val base = FullColor.fromHsl(90f, 0.8f, 0.5f)
        val rotated = base.rotateHue(0f)
        assertNear(base.toOkLch().L, rotated.toOkLch().L)
        assertNear(base.toOkLch().C, rotated.toOkLch().C, 0.01f)
    }

    @Test
    fun `rotateHue by 360 degrees returns equivalent color`() {
        val base = FullColor.fromHsl(45f, 0.9f, 0.4f)
        val rotated = base.rotateHue(360f)
        val (r1, g1, b1) = base.toRgb()
        val (r2, g2, b2) = rotated.toRgb()
        assertTrue(abs(r1 - r2) <= 2)
        assertTrue(abs(g1 - g2) <= 2)
        assertTrue(abs(b1 - b2) <= 2)
    }

    @Test
    fun `complement is 180 degree rotation`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val comp = base.complement()
        val baseH = base.toOkLch().H
        val compH = comp.toOkLch().H
        val d = abs(compH - baseH)
        val minAngularDiff = minOf(d, 360f - d)
        assertNear(180f, minAngularDiff, 2f, "Complement should be 180° rotated")
    }

    // ── setHue / complementary / splitComplementary / triadic / analogous ─────

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
    fun `setHue normalises out-of-range degrees`() {
        val base = FullColor.fromHsl(90f, 0.7f, 0.5f)
        assertNear(40f, base.setHue(400f).toOkLch().H, 3f, "setHue(400) should produce ~40°")
        assertNear(340f, base.setHue(-20f).toOkLch().H, 3f, "setHue(-20) should produce ~340°")
    }

    @Test
    fun `complementary matches complement`() {
        val base = FullColor.fromHsl(120f, 0.7f, 0.4f)
        assertEquals(base.complement().toRgb(), base.complementary().toRgb())
    }

    @Test
    fun `splitComplementary flanks are angle degrees from complement`() {
        val base = FullColor.fromHsl(60f, 0.8f, 0.5f)
        val (left, right) = base.splitComplementary(30f)
        val baseH = base.toOkLch().H
        assertNear(150f, (left.toOkLch().H - baseH + 360f) % 360f, 5f, "left split ~150° from base")
        assertNear(210f, (right.toOkLch().H - baseH + 360f) % 360f, 5f, "right split ~210° from base")
    }

    @Test
    fun `triadic colors are 120 degrees apart`() {
        val base = FullColor.fromHsl(0f, 0.8f, 0.5f)
        val (a, b, c) = base.triadic()
        assertEquals(base.toRgb(), a.toRgb(), "first element should equal base")
        assertNear(120f, (b.toOkLch().H - a.toOkLch().H + 360f) % 360f, 5f, "A→B gap ~120°")
        assertNear(120f, (c.toOkLch().H - b.toOkLch().H + 360f) % 360f, 5f, "B→C gap ~120°")
    }

    @Test
    fun `analogous center equals base and flanks are angle degrees away`() {
        val base = FullColor.fromHsl(180f, 0.8f, 0.5f)
        val (left, mid, right) = base.analogous(30f)
        assertNear(base.toOkLch().H, mid.toOkLch().H, 2f, "center should match base hue")
        assertNear(30f, (mid.toOkLch().H - left.toOkLch().H + 360f) % 360f, 3f, "left flank 30° behind")
        assertNear(30f, (right.toOkLch().H - mid.toOkLch().H + 360f) % 360f, 3f, "right flank 30° ahead")
    }

    // ── mix ───────────────────────────────────────────────────────────────────

    @Test
    fun `mix at t=0 returns first color`() {
        val a = FullColor.fromRgb(255, 0, 0)
        val b = FullColor.fromRgb(0, 0, 255)
        val mixed = a.mix(b, 0f)
        val (r, g, bl) = mixed.toRgb()
        assertEquals(255, r)
        assertEquals(0, g)
        assertEquals(0, bl)
    }

    @Test
    fun `mix at t=1 returns second color`() {
        val a = FullColor.fromRgb(255, 0, 0)
        val b = FullColor.fromRgb(0, 0, 255)
        val mixed = a.mix(b, 1f)
        val (r, g, bl) = mixed.toRgb()
        assertEquals(0, r)
        assertEquals(0, g)
        assertEquals(255, bl)
    }

    @Test
    fun `mix is symmetric around 0_5`() {
        val red = FullColor.fromRgb(200, 0, 0)
        val blue = FullColor.fromRgb(0, 0, 200)
        val midLab = red.mix(blue, 0.5f).toOkLab()
        val midLabReverse = blue.mix(red, 0.5f).toOkLab()
        assertNear(midLab.L, midLabReverse.L, 0.001f)
        assertNear(midLab.a, midLabReverse.a, 0.001f)
        assertNear(midLab.b, midLabReverse.b, 0.001f)
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
        assertEquals(a.mix(b, 0.3f).toRgb(), a.mixWith(b, 0.3f).toRgb())
    }

    // ── withAlpha ─────────────────────────────────────────────────────────────

    @Test
    fun `withAlpha sets alpha independently of color`() {
        val color = FullColor.fromRgb(100, 150, 200)
        val transparent = color.withAlpha(0.5f)
        assertNear(0.5f, transparent.alpha)
        assertEquals(color.toRgb(), transparent.toRgb())
    }

    // ── contrast / onColor / ensureContrast ──────────────────────────────────

    @Test
    fun `black on white has maximum contrast`() {
        val ratio = FullColor.BLACK.contrastRatio(FullColor.WHITE)
        assertNear(21f, ratio, 0.5f, "Black/white contrast should be near 21:1")
    }

    @Test
    fun `identical colors have contrast ratio 1`() {
        val color = FullColor.fromRgb(100, 100, 200)
        assertNear(1f, color.contrastRatio(color), 0.01f)
    }

    @Test
    fun `onColor returns black for white background`() {
        val (r, g, b) = FullColor.WHITE.onColor().toRgb()
        assertEquals(0, r, "onColor(white) should be black")
        assertEquals(0, g)
        assertEquals(0, b)
    }

    @Test
    fun `onColor returns white for black background`() {
        val (r, g, b) = FullColor.BLACK.onColor().toRgb()
        assertEquals(255, r, "onColor(black) should be white")
        assertEquals(255, g)
        assertEquals(255, b)
    }

    @Test
    fun `onColor uses custom light and dark`() {
        val customLight = FullColor.fromRgb(200, 200, 200)
        val customDark = FullColor.fromRgb(50, 50, 50)
        assertEquals(customLight.toRgb(), FullColor.BLACK.onColor(light = customLight, dark = customDark).toRgb())
    }

    @Test
    fun `ensureContrast leaves already-passing color unchanged`() {
        assertTrue(FullColor.BLACK.ensureContrast(FullColor.WHITE).contrastRatio(FullColor.WHITE) >= 4.5f)
    }

    @Test
    fun `ensureContrast raises low-contrast pair to meet minRatio`() {
        val fg = FullColor.fromRgb(128, 128, 128)
        val bg = FullColor.fromRgb(128, 128, 128)
        assertTrue(fg.ensureContrast(bg, 4.5f).contrastRatio(bg) >= 4.5f)
    }

    @Test
    fun `ensureContrast respects custom minRatio`() {
        val fg = FullColor.fromRgb(150, 100, 50)
        val bg = FullColor.fromRgb(130, 90, 40)
        assertTrue(fg.ensureContrast(bg, 3f).contrastRatio(bg) >= 3f)
    }
}

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

    // ── withAlpha ─────────────────────────────────────────────────────────────

    @Test
    fun `withAlpha sets alpha independently of color`() {
        val color = FullColor.fromRgb(100, 150, 200)
        val transparent = color.withAlpha(0.5f)
        assertNear(0.5f, transparent.alpha)
        assertEquals(color.toRgb(), transparent.toRgb())
    }

    // ── contrast ──────────────────────────────────────────────────────────────

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
}

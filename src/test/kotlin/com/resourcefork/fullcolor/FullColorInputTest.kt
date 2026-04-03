package com.resourcefork.fullcolor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** Tests for FullColor input factories. */
class FullColorInputTest {

    private fun assertNear(expected: Float, actual: Float, delta: Float = 1e-4f, message: String = "") {
        assertTrue(abs(actual - expected) <= delta, "$message: expected $expected but got $actual (delta $delta)")
    }

    // ── fromRgb ────────────────────────────────────────────────────────────────

    @Test
    fun `fromRgb red channel round trip`() {
        val color = FullColor.fromRgb(200, 50, 100)
        val (r, g, b) = color.toRgb()
        assertEquals(200, r)
        assertEquals(50, g)
        assertEquals(100, b)
    }

    @Test
    fun `fromRgb clamps channels`() {
        val color = FullColor.fromRgb(-10, 300, 128)
        val (r, g, b) = color.toRgb()
        assertEquals(0, r)
        assertEquals(255, g)
        assertEquals(128, b)
    }

    // ── fromRgba ──────────────────────────────────────────────────────────────

    @Test
    fun `fromRgba preserves alpha`() {
        val color = FullColor.fromRgba(255, 128, 0, 128)
        val rgba = color.toRgba()
        assertEquals(128, rgba.a)
    }

    // ── fromHex ───────────────────────────────────────────────────────────────

    @Test
    fun `fromHex 6-digit`() {
        val color = FullColor.fromHex("#FF8800")
        val (r, g, b) = color.toRgb()
        assertEquals(255, r)
        assertEquals(136, g)
        assertEquals(0, b)
    }

    @Test
    fun `fromHex 3-digit shorthand`() {
        val color = FullColor.fromHex("#F80")
        val (r, g, b) = color.toRgb()
        assertEquals(255, r)
        assertEquals(136, g)
        assertEquals(0, b)
    }

    @Test
    fun `fromHex 8-digit with alpha`() {
        val color = FullColor.fromHex("#FF880080")
        val rgba = color.toRgba()
        assertEquals(128, rgba.a)
    }

    @Test
    fun `fromHex without leading hash`() {
        val a = FullColor.fromHex("#AABBCC")
        val b = FullColor.fromHex("AABBCC")
        assertEquals(a.toRgb(), b.toRgb())
    }

    @Test
    fun `fromHex invalid throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            FullColor.fromHex("#ZZZZZZ")
        }
    }

    @Test
    fun `fromHex wrong length throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            FullColor.fromHex("#12345")
        }
    }

    // ── fromHsl ───────────────────────────────────────────────────────────────

    @Test
    fun `fromHsl red`() {
        val color = FullColor.fromHsl(0f, 1f, 0.5f)
        val (r, g, b) = color.toRgb()
        assertEquals(255, r)
        assertEquals(0, g)
        assertEquals(0, b)
    }

    @Test
    fun `fromHsl green`() {
        val color = FullColor.fromHsl(120f, 1f, 0.5f)
        val (r, g, b) = color.toRgb()
        assertEquals(0, r)
        assertEquals(255, g)
        assertEquals(0, b)
    }

    @Test
    fun `fromHsl blue`() {
        val color = FullColor.fromHsl(240f, 1f, 0.5f)
        val (r, g, b) = color.toRgb()
        assertEquals(0, r)
        assertEquals(0, g)
        assertEquals(255, b)
    }

    @Test
    fun `fromHsl grey has zero saturation`() {
        val color = FullColor.fromHsl(0f, 0f, 0.5f)
        val hsl = color.toHsl()
        assertNear(0f, hsl.s, 1e-3f)
    }

    @Test
    fun `fromHsl round trip`() {
        val h = 200f; val s = 0.6f; val l = 0.4f
        val color = FullColor.fromHsl(h, s, l)
        val hsl = color.toHsl()
        assertNear(h, hsl.h, 1f)
        assertNear(s, hsl.s, 0.01f)
        assertNear(l, hsl.l, 0.01f)
    }

    // ── fromHsv ───────────────────────────────────────────────────────────────

    @Test
    fun `fromHsv red`() {
        val color = FullColor.fromHsv(0f, 1f, 1f)
        val (r, g, b) = color.toRgb()
        assertEquals(255, r)
        assertEquals(0, g)
        assertEquals(0, b)
    }

    @Test
    fun `fromHsv round trip`() {
        val h = 150f; val s = 0.7f; val v = 0.9f
        val color = FullColor.fromHsv(h, s, v)
        val hsv = color.toHsv()
        assertNear(h, hsv.h, 1f)
        assertNear(s, hsv.s, 0.01f)
        assertNear(v, hsv.v, 0.01f)
    }

    // ── fromAndroidArgb ───────────────────────────────────────────────────────

    @Test
    fun `fromAndroidArgb opaque red`() {
        val argb = (0xFF shl 24) or (0xFF shl 16) // 0xFFFF0000
        val color = FullColor.fromAndroidArgb(argb)
        val (r, g, b) = color.toRgb()
        assertEquals(255, r)
        assertEquals(0, g)
        assertEquals(0, b)
        assertNear(1f, color.alpha)
    }

    @Test
    fun `fromAndroidArgb round trip`() {
        val original = 0x80_10_20_30.toInt() // semi-transparent
        val color = FullColor.fromAndroidArgb(original)
        // Check alpha channel approximately
        assertNear(0x80 / 255f, color.alpha, 0.01f)
    }

    // ── fromRgbFloat ──────────────────────────────────────────────────────────

    @Test
    fun `fromRgbFloat round trip`() {
        val color = FullColor.fromRgbFloat(0.5f, 0.25f, 0.75f)
        val (r, g, b) = color.toRgbFloat()
        assertNear(0.5f, r, 0.01f)
        assertNear(0.25f, g, 0.01f)
        assertNear(0.75f, b, 0.01f)
    }

    // ── toHex ─────────────────────────────────────────────────────────────────

    @Test
    fun `toHex returns uppercase 6-digit format`() {
        val hex = FullColor.fromRgb(255, 136, 0).toHex()
        assertEquals("#FF8800", hex)
    }

    @Test
    fun `toHex includes alpha when requested and not opaque`() {
        val hex = FullColor.fromRgba(255, 0, 0, 128).toHex(includeAlpha = true)
        assertTrue(hex.length == 9, "Expected 9-char hex but got: $hex")
    }

    @Test
    fun `toHex no alpha suffix when fully opaque even if requested`() {
        val hex = FullColor.fromRgb(0, 255, 0).toHex(includeAlpha = true)
        assertEquals("#00FF00", hex)
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Test
    fun `toString delegates to toHex`() {
        val color = FullColor.fromRgb(10, 20, 30)
        assertEquals(color.toHex(), color.toString())
    }
}

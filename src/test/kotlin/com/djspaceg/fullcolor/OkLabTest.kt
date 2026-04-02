package com.djspaceg.fullcolor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

/** Tests for OKLab ↔ OKLch conversion math. */
class OkLabTest {

    private fun assertNear(expected: Float, actual: Float, delta: Float = 1e-4f, message: String = "") {
        assertTrue(abs(actual - expected) <= delta, "$message: expected $expected but got $actual (delta $delta)")
    }

    @Test
    fun `black in OKLab has L=0`() {
        val black = FullColor.fromRgb(0, 0, 0).toOkLab()
        assertNear(0f, black.L, message = "L of black")
    }

    @Test
    fun `white in OKLab has L=1`() {
        val white = FullColor.fromRgb(255, 255, 255).toOkLab()
        assertNear(1f, white.L, message = "L of white")
    }

    @Test
    fun `grey in OKLab has a=0 and b=0`() {
        val grey = FullColor.fromRgb(128, 128, 128).toOkLab()
        assertNear(0f, grey.a, 1e-3f, "a of grey")
        assertNear(0f, grey.b, 1e-3f, "b of grey")
    }

    @Test
    fun `round trip OKLab to sRGB stays within 1 unit`() {
        val original = FullColor.fromRgb(100, 149, 237) // cornflower blue
        val (r, g, b) = original.toRgb()
        assertEquals(100, r, "red channel")
        assertEquals(149, g, "green channel")
        assertEquals(237, b, "blue channel")
    }

    @Test
    fun `OKLab lerp at t=0 equals start`() {
        val a = OkLab(0.5f, 0.1f, -0.1f)
        val b = OkLab(0.8f, -0.05f, 0.2f)
        assertEquals(a, a.lerp(b, 0f))
    }

    @Test
    fun `OKLab lerp at t=1 equals end`() {
        val a = OkLab(0.5f, 0.1f, -0.1f)
        val b = OkLab(0.8f, -0.05f, 0.2f)
        val result = a.lerp(b, 1f)
        assertNear(b.L, result.L)
        assertNear(b.a, result.a)
        assertNear(b.b, result.b)
    }

    @Test
    fun `OKLab lerp at t=0_5 is midpoint`() {
        val a = OkLab(0f, 0f, 0f)
        val b = OkLab(1f, 0.2f, -0.2f)
        val mid = a.lerp(b, 0.5f)
        assertNear(0.5f, mid.L)
        assertNear(0.1f, mid.a)
        assertNear(-0.1f, mid.b)
    }

    @Test
    fun `OKLab to OKLch round trip`() {
        val lab = OkLab(0.7f, 0.1f, 0.15f)
        val lch = lab.toOkLch()
        val backToLab = lch.toOkLab()
        assertNear(lab.L, backToLab.L)
        assertNear(lab.a, backToLab.a, 1e-4f)
        assertNear(lab.b, backToLab.b, 1e-4f)
    }
}

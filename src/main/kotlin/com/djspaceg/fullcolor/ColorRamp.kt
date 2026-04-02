package com.djspaceg.fullcolor

/**
 * Generates a multi-step color ramp between two [FullColor] values.
 *
 * Interpolation is performed in [OkLab] space by default, which produces
 * perceptually uniform transitions. Optionally, interpolation can be done
 * in [OkLch] space (cylindrical), which preserves chroma and produces more
 * vivid mid-points but may introduce hue-shift artifacts for complementary colors.
 */
object ColorRamp {

    /**
     * Generate a [steps]-step ramp from [start] to [end] (inclusive) in OKLab space.
     *
     * @param start The first color in the ramp.
     * @param end The last color in the ramp.
     * @param steps Total number of colors in the output list (minimum 2).
     * @return A list of [steps] colors evenly distributed from [start] to [end].
     */
    fun generate(start: FullColor, end: FullColor, steps: Int): List<FullColor> {
        require(steps >= 2) { "steps must be at least 2, got $steps" }
        return List(steps) { i ->
            val t = i / (steps - 1).toFloat()
            FullColor.fromOkLab(start.lab.lerp(end.lab, t), start.alpha + (end.alpha - start.alpha) * t)
        }
    }

    /**
     * Generate a [steps]-step ramp in OKLch space (hue-preserving).
     *
     * @param start The first color in the ramp.
     * @param end The last color in the ramp.
     * @param steps Total number of colors in the output list (minimum 2).
     * @return A list of [steps] colors evenly distributed from [start] to [end].
     */
    fun generateLch(start: FullColor, end: FullColor, steps: Int): List<FullColor> {
        require(steps >= 2) { "steps must be at least 2, got $steps" }
        val startLch = start.toOkLch()
        val endLch = end.toOkLch()
        return List(steps) { i ->
            val t = i / (steps - 1).toFloat()
            val lch = startLch.lerp(endLch, t)
            val alpha = start.alpha + (end.alpha - start.alpha) * t
            FullColor.fromOkLch(lch, alpha)
        }
    }
}

/**
 * A multi-stop color gradient that distributes colors between any number of control points.
 *
 * Stops are defined as (position in [0, 1], [FullColor]) pairs. Interpolation between
 * adjacent stops is performed in OKLab space.
 */
class ColorGradient(stops: List<Pair<Float, FullColor>>) {

    private val sortedStops: List<Pair<Float, FullColor>> =
        stops.sortedBy { it.first }.also { sorted ->
            require(sorted.size >= 2) { "A gradient requires at least 2 color stops." }
            require(sorted.all { it.first in 0f..1f }) {
                "Color stop positions must be within [0, 1]. Got: ${sorted.map { it.first }}"
            }
        }

    /**
     * Sample the gradient at position [t] (0 = first stop, 1 = last stop).
     */
    fun sample(t: Float): FullColor {
        val tClamped = t.coerceIn(0f, 1f)

        // Find the two surrounding stops
        val nextIndex = sortedStops.indexOfFirst { it.first >= tClamped }
        return when {
            nextIndex == 0 -> sortedStops.first().second
            nextIndex == -1 -> sortedStops.last().second
            else -> {
                val (p0, c0) = sortedStops[nextIndex - 1]
                val (p1, c1) = sortedStops[nextIndex]
                val localT = if (p1 - p0 < 1e-6f) 0f else (tClamped - p0) / (p1 - p0)
                c0.mix(c1, localT)
            }
        }
    }

    /**
     * Sample the gradient at [count] evenly-spaced positions from 0 to 1 inclusive.
     */
    fun sample(count: Int): List<FullColor> {
        require(count >= 2) { "count must be at least 2, got $count" }
        return List(count) { i -> sample(i / (count - 1).toFloat()) }
    }

    companion object {
        /**
         * Create a gradient from a vararg list of colors evenly spaced from 0 to 1.
         */
        fun of(vararg colors: FullColor): ColorGradient {
            require(colors.size >= 2) { "A gradient requires at least 2 colors." }
            val step = 1f / (colors.size - 1).toFloat()
            val stops = colors.mapIndexed { i, c -> (i * step) to c }
            return ColorGradient(stops)
        }
    }
}

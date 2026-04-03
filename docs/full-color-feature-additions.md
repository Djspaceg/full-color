# full-color Feature Additions

This document describes the features added in **full-color 1.1.0** that enable
downstream projects (such as _dictifind_) to replace custom color utilities with
this library.

---

## 1 · `FullColor.kt` — new manipulation methods

All new manipulation methods live directly inside `FullColor` alongside their
existing siblings, rather than in a separate file.

### Absolute setters

Absolute setters let callers pin an OKLch/OKLab axis to an exact value while
preserving the others. They complement the existing relative modifiers
(`lighten`, `darken`, `rotateHue`, etc.).

| Function | Description |
|---|---|
| `setHue(degrees: Float): FullColor` | Set the OKLch hue to an absolute angle (0–360°, normalized). Lightness and chroma are preserved. |
| `setLightness(lightness: Float): FullColor` | Set the OKLab lightness to an absolute value (0–1, clamped). The a/b chroma axes are preserved. |
| `setChroma(chroma: Float): FullColor` | Set the OKLch chroma to an absolute value (≥ 0, clamped). Lightness and hue are preserved. |

### Relative adjusters

These complement the existing unsigned `lighten`/`darken` and
`saturate`/`desaturate` pairs with signed and additive variants where they
offer genuinely distinct behaviour.

| Function | Description |
|---|---|
| `adjustLightness(amount: Float): FullColor` | Shift OKLab L by a signed offset (positive = lighter, negative = darker). Unlike `lighten`/`darken`, this never jumps to pure white/black — it simply clamps L to [0, 1] while preserving the a/b axes. |
| `adjustChroma(amount: Float): FullColor` | **Additive** chroma shift. Unlike `saturate`/`desaturate` (which scale chroma proportionally), this shifts OKLch C by an absolute amount. Clamped to ≥ 0. |

### Accessibility helpers

| Function | Description |
|---|---|
| `onColor(light, dark): FullColor` | Return whichever of `light` (default white) or `dark` (default black) achieves higher WCAG contrast against this background. Useful for legible text colors on dynamic backgrounds. |
| `ensureContrast(background, minRatio): FullColor` | Return this color, or a lightness-adjusted version, that meets `minRatio` WCAG contrast against `background` (default 4.5 : 1 for WCAG AA). Shifts lightness in the direction that can achieve the higher maximum contrast. |

### Color-harmony methods

`splitComplementary`, `triadic`, and `analogous` were previously only available
as static `ColorUtils.*` calls. They are now also available as instance methods
so callers do not need to import `ColorUtils` for harmony operations.

| Function | Description |
|---|---|
| `splitComplementary(angle: Float = 30f): Pair<FullColor, FullColor>` | Two colors each `angle` degrees away from the complementary hue. |
| `triadic(): Triple<FullColor, FullColor, FullColor>` | Three colors evenly spaced at 120° intervals; this color is the first element. |
| `analogous(angle: Float = 30f): Triple<FullColor, FullColor, FullColor>` | Three colors separated by `angle` degrees; this color is the center element. |

---

## 2 · `ColorUtils.kt` — delegation to instance methods

`ColorUtils.splitComplement`, `triadic`, `analogous`, and `bestContrast` now
delegate to the corresponding `FullColor` instance methods, so there is a
single source of truth for the harmony and contrast logic.

---

## 3 · `full-color-compose` module — `ComposeColorExtensions.kt`

> **Status:** planned for a future release that adds an Android/Compose target.

A separate Gradle module (`com.resourcefork:full-color-compose`) will add
extension functions on `androidx.compose.ui.graphics.Color`. The
implementations delegate to the `FullColor` API above.

### Bridge conversions

```kotlin
fun Color.toFullColor(): FullColor
fun FullColor.toComposeColor(): Color
```

### Forwarded extensions on `Color`

```kotlin
fun Color.setHue(degrees: Float): Color
fun Color.setLightness(lightness: Float): Color
fun Color.setChroma(chroma: Float): Color

fun Color.adjustLightness(amount: Float): Color
fun Color.adjustChroma(amount: Float): Color

fun Color.onColor(light: Color = Color.White, dark: Color = Color.Black): Color
fun Color.ensureContrast(background: Color, minRatio: Float = 4.5f): Color

fun Color.complement(): Color
fun Color.splitComplementary(angle: Float = 30f): Pair<Color, Color>
fun Color.triadic(): Triple<Color, Color, Color>
fun Color.analogous(angle: Float = 30f): Triple<Color, Color, Color>
fun Color.mix(other: Color, ratio: Float = 0.5f): Color
```

Contrast/luminance utilities from existing `FullColor` methods are also exposed:

```kotlin
fun Color.luminance(): Float               // WCAG relative luminance
fun Color.contrastRatio(other: Color): Float
fun Color.rotateHue(degrees: Float): Color
```

---

## 4 · Migration checklist for consumers

1. Add `com.resourcefork:full-color:1.1.0` (or later) to your dependencies.
2. Replace direct `colorkt` usage with `FullColor` equivalents.
3. For Compose projects, wait for `com.resourcefork:full-color-compose` and
   add that artifact; all extension function names are intentionally identical
   to the planned `ComposeColorExtensions` API.
4. Delete any local reimplementations of the functions listed above.

---

## 5 · Version history

| Version | Changes |
|---|---|
| 1.0.0 | Initial release: `FullColor`, `OkLab`, `OkLch`, `ColorRamp`, `ColorGradient`, `ColorUtils`. |
| 1.1.0 | Added absolute setters (`setHue`, `setLightness`, `setChroma`), relative adjusters (`adjustLightness`, `adjustChroma`), accessibility helpers (`onColor`, `ensureContrast`), and harmony instance methods (`splitComplementary`, `triadic`, `analogous`) directly to `FullColor`. `ColorUtils` harmony/contrast functions now delegate to `FullColor` instance methods. |

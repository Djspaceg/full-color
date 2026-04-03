# full-color Feature Additions

This document describes the features added in **full-color 1.1.0** that enable
downstream projects (such as _dictifind_) to replace custom color utilities with
this library.

---

## 1 · `SrgbColorExtensions.kt` (pure-Kotlin module)

A new file `SrgbColorExtensions.kt` (package `com.resourcefork.fullcolor`) adds
extension functions directly on `FullColor`. These form the implementation layer
that the `full-color-compose` bridge will surface on
`androidx.compose.ui.graphics.Color`.

### Absolute OKLch setters

| Function | Description |
|---|---|
| `FullColor.setHue(degrees: Float): FullColor` | Set the OKLch hue to an absolute angle (0–360°). |
| `FullColor.setLightness(L: Float): FullColor` | Set the OKLab lightness to an absolute value (0–1). |
| `FullColor.setChroma(C: Float): FullColor` | Set the OKLch chroma to an absolute value (≥ 0). |

### Relative adjusters

| Function | Description |
|---|---|
| `FullColor.adjustLightness(amount: Float): FullColor` | Shift OKLab L by a signed offset (positive = lighter). Clamped to [0, 1]. |
| `FullColor.adjustSaturation(amount: Float): FullColor` | Scale chroma by `(1 + amount)`. `-1` = fully desaturated, `+0.5` = 50 % more vivid. |
| `FullColor.adjustChroma(amount: Float): FullColor` | Add `amount` to the OKLch chroma directly. Clamped to ≥ 0. |

### Accessibility helpers

| Function | Description |
|---|---|
| `FullColor.onColor(light, dark): FullColor` | Return whichever of `light` (default white) or `dark` (default black) has higher contrast against this background. |
| `FullColor.ensureContrast(background, minRatio): FullColor` | Return this color or a lightness-adjusted version that meets `minRatio` WCAG contrast against `background` (default 4.5 : 1). |

### Color-harmony shortcuts

These are convenience aliases and instance-level wrappers for operations that
previously required `ColorUtils.*` calls.

| Function | Equivalent |
|---|---|
| `FullColor.complementary(): FullColor` | `FullColor.complement()` |
| `FullColor.splitComplementary(angle): Pair<FullColor, FullColor>` | `ColorUtils.splitComplement(this, angle)` |
| `FullColor.triadic(): Triple<FullColor, FullColor, FullColor>` | `ColorUtils.triadic(this)` |
| `FullColor.analogous(angle): Triple<FullColor, FullColor, FullColor>` | `ColorUtils.analogous(this, angle)` |
| `FullColor.mixWith(other, ratio): FullColor` | `FullColor.mix(other, ratio)` |

---

## 2 · `full-color-compose` module — `ComposeColorExtensions.kt`

> **Status:** planned for a future release that adds an Android/Compose target.

A separate Gradle module (`com.resourcefork:full-color-compose`) will add the
following extension functions on `androidx.compose.ui.graphics.Color`. The
implementations delegate to the `FullColor` API above.

### Bridge conversions

```kotlin
fun Color.toFullColor(): FullColor
fun FullColor.toComposeColor(): Color
```

### Forwarded extensions on `Color`

Every function from §1 will be available directly on `Color` by converting to
`FullColor`, applying the operation, and converting back:

```kotlin
fun Color.setHue(degrees: Float): Color
fun Color.setLightness(L: Float): Color
fun Color.setChroma(C: Float): Color

fun Color.adjustLightness(amount: Float): Color
fun Color.adjustSaturation(amount: Float): Color
fun Color.adjustChroma(amount: Float): Color

fun Color.onColor(light: Color = Color.White, dark: Color = Color.Black): Color
fun Color.ensureContrast(background: Color, minRatio: Float = 4.5f): Color

fun Color.complementary(): Color
fun Color.splitComplementary(angle: Float = 30f): Pair<Color, Color>
fun Color.triadic(): Triple<Color, Color, Color>
fun Color.analogous(angle: Float = 30f): Triple<Color, Color, Color>
fun Color.mixWith(other: Color, ratio: Float = 0.5f): Color
```

Contrast/luminance utilities from existing `FullColor` methods are also exposed:

```kotlin
fun Color.luminance(): Float               // WCAG relative luminance
fun Color.contrastRatio(other: Color): Float
fun Color.rotateHue(degrees: Float): Color
```

---

## 3 · Migration checklist for consumers

1. Add `com.resourcefork:full-color:1.1.0` (or later) to your dependencies.
2. Replace direct `colorkt` usage with `FullColor` equivalents.
3. For Compose projects, wait for `com.resourcefork:full-color-compose` and
   add that artifact; all extension function names are intentionally identical
   to the planned `ComposeColorExtensions` API.
4. Delete any local reimplementations of the functions listed above.

---

## 4 · Version history

| Version | Changes |
|---|---|
| 1.0.0 | Initial release: `FullColor`, `OkLab`, `OkLch`, `ColorRamp`, `ColorGradient`, `ColorUtils`. |
| 1.1.0 | Added `SrgbColorExtensions.kt`: absolute setters, relative adjusters, accessibility helpers, and color-harmony shortcuts on `FullColor`. |

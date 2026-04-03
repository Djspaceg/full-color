# full-color Feature Additions

This document describes the features added in **full-color 1.1.0** that enable
downstream projects (such as _dictifind_) to replace custom color utilities with
this library.

---

## Naming convention

All manipulation methods follow a **consistent prefix-based convention** keyed to
the category of operation. The same convention applies uniformly to every color
axis (`lightness`, `chroma`, `hue`, `alpha`):

| Category | Pattern | Description |
|---|---|---|
| **Read** | `.{axis}` property | Returns the current axis value. |
| **Absolute set** | `with{Axis}(value)` | Returns a copy with the axis set to an exact value. |
| **Signed relative adjust** | `adjust{Axis}(amount)` | Returns a copy with the axis shifted by a signed offset. |
| **Relative up** (unsigned) | English color vocabulary | Returns a copy with the axis increased by a positive magnitude. |
| **Relative down** (unsigned) | English color vocabulary | Returns a copy with the axis decreased by a positive magnitude. |

The `with` prefix for absolute setters is established by the original `withAlpha`
method; all new setters follow the same pattern.

---

## 1 · `FullColor.kt` — new manipulation methods

### Axis readers (computed properties)

Direct property access to the OKLch/OKLab axis values, completing the read side
of the naming convention alongside the existing `alpha` property.

| Property | Description |
|---|---|
| `val lightness: Float` | OKLab lightness in [0, 1]. |
| `val chroma: Float` | OKLch chroma (≥ 0). |
| `val hue: Float` | OKLch hue in [0, 360). |

### Absolute setters

Return a copy with one axis pinned to an exact value while the others are
preserved. These complete the `with{Axis}` family alongside the existing
`withAlpha`.

| Function | Description |
|---|---|
| `withHue(degrees: Float): FullColor` | Set the OKLch hue to an absolute angle (0–360°, normalized). Lightness and chroma are preserved. |
| `withLightness(lightness: Float): FullColor` | Set the OKLab lightness to an absolute value (0–1, clamped). The a/b chroma axes are preserved. |
| `withChroma(chroma: Float): FullColor` | Set the OKLch chroma to an absolute value (≥ 0, clamped). Lightness and hue are preserved. |

### Signed relative adjusters

Return a copy with one axis shifted by a signed offset. These complete the
`adjust{Axis}` family.

| Function | Description |
|---|---|
| `adjustHue(degrees: Float): FullColor` | Shift OKLch hue by a signed offset (positive or negative, wraps around 360°). |
| `adjustLightness(amount: Float): FullColor` | Shift OKLab L by a signed offset. Clamped to [0, 1]. Unlike `lighten`/`darken`, this never jumps to pure white/black — it only shifts L while preserving the a/b chroma axes. |
| `adjustChroma(amount: Float): FullColor` | Add `amount` to the OKLch chroma (additive, not multiplicative). Clamped to ≥ 0. |

### Relative up/down (existing, unchanged)

Established English color vocabulary for unsigned increases and decreases.
`lighten`/`darken` have a special boundary behavior: an `amount` of `1`
returns pure `WHITE`/`BLACK` (zeroing out OKLab a/b), which is distinct from
`adjustLightness` at the same magnitude.

| Function | Category | Description |
|---|---|---|
| `lighten(amount)` | relative up | Shift L upward; `1` → pure white. |
| `darken(amount)` | relative down | Shift L downward; `1` → pure black. |
| `saturate(amount)` | relative up | Scale OKLch chroma upward (multiplicative). |
| `desaturate(amount)` | relative down | Scale OKLch chroma downward (multiplicative). |

### Accessibility helpers

| Function | Description |
|---|---|
| `onColor(light, dark): FullColor` | Return whichever of `light` (default white) or `dark` (default black) achieves higher WCAG contrast against this background. |
| `ensureContrast(background, minRatio): FullColor` | Return this color, or a lightness-adjusted version, that meets `minRatio` WCAG contrast against `background` (default 4.5 : 1 for WCAG AA). |

### Color-harmony methods

`splitComplementary`, `triadic`, and `analogous` are now available as instance
methods in addition to the existing `ColorUtils.*` static entry-points.

| Function | Description |
|---|---|
| `splitComplementary(angle: Float = 30f): Pair<FullColor, FullColor>` | Two colors each `angle` degrees away from the complementary hue. |
| `triadic(): Triple<FullColor, FullColor, FullColor>` | Three colors evenly spaced at 120° intervals; this color is the first element. |
| `analogous(angle: Float = 30f): Triple<FullColor, FullColor, FullColor>` | Three colors separated by `angle` degrees; this color is the center element. |

---

## 2 · Internal architecture

All OKLch/OKLab axis mutations now route through two private meta-utility helpers:

```kotlin
private fun modifyLab(transform: (OkLab) -> OkLab): FullColor
private fun modifyLch(transform: (OkLch) -> OkLch): FullColor
```

This ensures every axis transformation goes through a consistent path, prevents
accidental divergence in how colors are reconstructed, and removes the boilerplate
of manually calling `lab.toOkLch()`, mutating, then calling `.toOkLab()`.

---

## 3 · `ColorUtils.kt` — delegation to instance methods

`ColorUtils.splitComplement`, `triadic`, `analogous`, and `bestContrast` delegate
to the corresponding `FullColor` instance methods, so there is a single source of
truth for the harmony and contrast logic.

`ColorUtils.tetradic` now uses `adjustHue` in line with the new naming convention.

---

## 4 · `full-color-compose` module — `ComposeColorExtensions.kt`

> **Status:** planned for a future release that adds an Android/Compose target.

A separate Gradle module (`com.resourcefork:full-color-compose`) will add
extension functions on `androidx.compose.ui.graphics.Color`. The
implementations delegate to the `FullColor` API above, and use the same
naming convention.

### Bridge conversions

```kotlin
fun Color.toFullColor(): FullColor
fun FullColor.toComposeColor(): Color
```

### Forwarded extensions on `Color`

```kotlin
// Axis readers
val Color.lightness: Float
val Color.chroma: Float
val Color.hue: Float

// Absolute setters
fun Color.withHue(degrees: Float): Color
fun Color.withLightness(lightness: Float): Color
fun Color.withChroma(chroma: Float): Color

// Signed relative adjusters
fun Color.adjustHue(degrees: Float): Color
fun Color.adjustLightness(amount: Float): Color
fun Color.adjustChroma(amount: Float): Color

// Accessibility
fun Color.onColor(light: Color = Color.White, dark: Color = Color.Black): Color
fun Color.ensureContrast(background: Color, minRatio: Float = 4.5f): Color

// Harmony
fun Color.complement(): Color
fun Color.splitComplementary(angle: Float = 30f): Pair<Color, Color>
fun Color.triadic(): Triple<Color, Color, Color>
fun Color.analogous(angle: Float = 30f): Triple<Color, Color, Color>
fun Color.mix(other: Color, ratio: Float = 0.5f): Color

// Contrast/luminance
fun Color.luminance(): Float
fun Color.contrastRatio(other: Color): Float
```

---

## 5 · Migration checklist for consumers

1. Add `com.resourcefork:full-color:1.1.0` (or later) to your dependencies.
2. Replace direct `colorkt` usage with `FullColor` equivalents.
3. Rename any calls to the old names: `setLightness` → `withLightness`,
   `setChroma` → `withChroma`, `setHue` → `withHue`, `rotateHue` → `adjustHue`.
4. For Compose projects, wait for `com.resourcefork:full-color-compose`.
5. Delete any local reimplementations of the functions listed above.

---

## 6 · Version history

| Version | Changes |
|---|---|
| 1.0.0 | Initial release: `FullColor`, `OkLab`, `OkLch`, `ColorRamp`, `ColorGradient`, `ColorUtils`. |
| 1.1.0 | Established consistent `with{Axis}`/`adjust{Axis}`/`.{axis}` naming convention. Added axis readers (`.lightness`, `.chroma`, `.hue`), absolute setters (`withLightness`, `withChroma`, `withHue`), signed relative adjusters (`adjustLightness`, `adjustChroma`, `adjustHue`), accessibility helpers (`onColor`, `ensureContrast`), and harmony instance methods (`splitComplementary`, `triadic`, `analogous`). Internal `modifyLab`/`modifyLch` helpers provide a consistent axis-mutation path. |

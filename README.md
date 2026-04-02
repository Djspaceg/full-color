# full-color

A Kotlin-based color utility library with wide-gamut **OKLab** color space support, suitable for use in Java and Kotlin projects.

## Features

- **Wide-gamut internal representation** — colors are stored internally as [OKLab](https://bottosson.github.io/posts/oklab/) values, a perceptually uniform color space ideal for manipulation and interpolation.
- **Multiple input formats**:
  - Hex strings (`#RGB`, `#RGBA`, `#RRGGBB`, `#RRGGBBAA`)
  - Integer sRGB / sRGBA (0–255 per channel)
  - Floating-point sRGB (0.0–1.0)
  - HSL and HSV
  - Android packed ARGB integers (compatible with `android.graphics.Color`)
  - Jetpack Compose packed color longs (compatible with `androidx.compose.ui.graphics.Color`)
  - CSS color strings (`rgb()`, `rgba()`, `hsl()`, `hsla()`, named colors)
- **Multiple output formats** — hex, sRGB, HSL, HSV, OKLab, OKLch, Android ARGB
- **Perceptual color manipulation** — lighten, darken, saturate, desaturate, hue rotation, complement, mix
- **WCAG accessibility** — relative luminance and contrast ratio (AA / AAA compliance helpers)
- **Color ramps** — perceptual linear ramps in OKLab or OKLch space
- **Multi-stop gradients** — arbitrary stop positions with OKLab interpolation
- **Color harmonies** — complementary, split-complementary, analogous, triadic, tetradic, monochromatic palettes
- **CSS color parsing** — `ColorUtils.parseCss()`
- **Maven-compatible** — published with `maven-publish`, group `com.djspaceg`, artifact `full-color`

## Usage

### Add to your project

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("com.djspaceg:full-color:1.0.0")
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.djspaceg</groupId>
    <artifactId>full-color</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Quick examples

```kotlin
import com.djspaceg.fullcolor.*

// Create colors from various sources
val red      = FullColor.fromHex("#FF0000")
val blue     = FullColor.fromRgb(0, 0, 255)
val green    = FullColor.fromHsl(120f, 1f, 0.5f)
val android  = FullColor.fromAndroidArgb(0xFF_00_FF_00.toInt())
val css      = ColorUtils.parseCss("rgba(255, 128, 0, 0.8)")

// Manipulate
val lighter  = red.lighten(0.2f)
val rotated  = blue.rotateHue(90f)
val mixed    = red.mix(blue, 0.5f)

// Output
println(red.toHex())            // #FF0000
println(red.toAndroidArgb())    // -65536
println(red.toOkLab())          // OkLab(L=0.6279554, a=0.22486308, b=0.12584576)

// Color ramp (10 steps from red to blue, perceptual interpolation)
val ramp = ColorRamp.generate(red, blue, 10)

// Multi-stop gradient
val sunset = ColorGradient.of(
    FullColor.fromHex("#FF4500"),
    FullColor.fromHex("#FF8C00"),
    FullColor.fromHex("#FFD700"),
)
val midColor = sunset.sample(0.5f)

// Color harmonies
val (a, b2, c) = ColorUtils.triadic(red)
val palette    = ColorUtils.monochromaticPalette(green, steps = 7)

// Accessibility
val ratio   = FullColor.BLACK.contrastRatio(FullColor.WHITE) // ~21
val okForAA = ColorUtils.isWcagAaCompliant(FullColor.BLACK, FullColor.WHITE) // true
val best    = ColorUtils.bestContrast(red) // picks black or white for text
```

## Building

```bash
./gradlew build
./gradlew test
./gradlew publishToMavenLocal
```

Requires Java 17+.

## License

MIT

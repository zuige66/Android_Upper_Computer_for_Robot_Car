package com.carhost.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.carhost.mobile.data.model.ColorTheme

// —— Morandi palette: muted, low-saturation, harmonious tones ————
// Each theme defines 3 key colors:
//   surface  = top half of swatch → top bar + bottom nav
//   primary  = bottom-left of swatch → card backgrounds
//   tertiary = bottom-right of swatch → capsule elements

// ========== 默认 灰蓝灰 ==========
private val DefaultDark = darkColorScheme(
    background = Color(0xFF15120E),
    surface = Color(0xFF1D1A16),
    surfaceContainer = Color(0xFF1D1A16),
    surfaceContainerLow = Color(0xFF2A2640),
    surfaceVariant = Color(0xFF36322C),
    primaryContainer = Color(0xFFB8AED4),
    onPrimaryContainer = Color(0xFF1E1830),
    primary = Color(0xFF9A93CE),
    tertiary = Color(0xFFB8AED4),
    secondary = Color(0xFF8E8A9E),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF2A2550),
    onTertiary = Color(0xFF2E2550),
    onSecondary = Color(0xFF2E2A3E),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFCAC2B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
    errorContainer = Color(0xFF5F140D),
    onErrorContainer = Color(0xFFFFB4A8),
)

// ========== 红黄粉 ==========
private val RedYellowPinkDark = darkColorScheme(
    background = Color(0xFF181212),
    surface = Color(0xFF2D2222),
    surfaceContainer = Color(0xFF2D2222),
    surfaceContainerLow = Color(0xFF4A2828),
    surfaceVariant = Color(0xFF3D302E),
    primaryContainer = Color(0xFFD4B0B8),
    onPrimaryContainer = Color(0xFF2A1818),
    primary = Color(0xFFD4A898),
    tertiary = Color(0xFFD4B0B8),
    secondary = Color(0xFFB89890),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF3A2018),
    onTertiary = Color(0xFF3A2028),
    onSecondary = Color(0xFF2E1E1A),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFCAB8B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
    errorContainer = Color(0xFF5F140D),
    onErrorContainer = Color(0xFFFFB4A8),
)

// ========== 黄绿灰 ==========
private val YellowGreenGrayDark = darkColorScheme(
    background = Color(0xFF181A14),
    surface = Color(0xFF242822),
    surfaceContainer = Color(0xFF242822),
    surfaceContainerLow = Color(0xFF304028),
    surfaceVariant = Color(0xFF343830),
    primaryContainer = Color(0xFFA8AEA8),
    onPrimaryContainer = Color(0xFF1A2018),
    primary = Color(0xFFA8BC90),
    tertiary = Color(0xFFA8AEA8),
    secondary = Color(0xFF8E9E82),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF1E3018),
    onTertiary = Color(0xFF1E2A1E),
    onSecondary = Color(0xFF1E2A1A),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFC4C4B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
    errorContainer = Color(0xFF5F140D),
    onErrorContainer = Color(0xFFFFB4A8),
)

// ========== 蓝绿灰 ==========
private val BlueGreenGrayDark = darkColorScheme(
    background = Color(0xFF161A1C),
    surface = Color(0xFF22282A),
    surfaceContainer = Color(0xFF22282A),
    surfaceContainerLow = Color(0xFF283840),
    surfaceVariant = Color(0xFF32383A),
    primaryContainer = Color(0xFFA8B4B8),
    onPrimaryContainer = Color(0xFF182028),
    primary = Color(0xFF80B8C0),
    tertiary = Color(0xFFA8B4B8),
    secondary = Color(0xFF7098A0),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF10303A),
    onTertiary = Color(0xFF1A2E3A),
    onSecondary = Color(0xFF142A32),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFBCC4C4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
    errorContainer = Color(0xFF5F140D),
    onErrorContainer = Color(0xFFFFB4A8),
)

// ========== 黑白灰 ==========
private val BlackWhiteGrayDark = darkColorScheme(
    background = Color(0xFF121212),
    surface = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerLow = Color(0xFF2A2A2A),
    surfaceVariant = Color(0xFF2A2A2A),
    primaryContainer = Color(0xFF989898),
    onPrimaryContainer = Color(0xFF1A1A1A),
    primary = Color(0xFFC8C8C8),
    tertiary = Color(0xFF989898),
    secondary = Color(0xFFA8A8A8),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF2E2E2E),
    onTertiary = Color(0xFF2E2E2E),
    onSecondary = Color(0xFF2E2E2E),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFC4C4C4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
    errorContainer = Color(0xFF5F140D),
    onErrorContainer = Color(0xFFFFB4A8),
)

// —— Swatch colors for the picker UI (the 3 Morandi colors per theme) ————
data class ThemeSwatchColors(
    val surface: Color,
    val primary: Color,
    val tertiary: Color,
)

val themeSwatches = mapOf(
    ColorTheme.Default to ThemeSwatchColors(
        surface = DefaultDark.surfaceContainer,
        primary = DefaultDark.surfaceContainerLow,
        tertiary = DefaultDark.primaryContainer,
    ),
    ColorTheme.RedYellowPink to ThemeSwatchColors(
        surface = RedYellowPinkDark.surfaceContainer,
        primary = RedYellowPinkDark.surfaceContainerLow,
        tertiary = RedYellowPinkDark.primaryContainer,
    ),
    ColorTheme.YellowGreenGray to ThemeSwatchColors(
        surface = YellowGreenGrayDark.surfaceContainer,
        primary = YellowGreenGrayDark.surfaceContainerLow,
        tertiary = YellowGreenGrayDark.primaryContainer,
    ),
    ColorTheme.BlueGreenGray to ThemeSwatchColors(
        surface = BlueGreenGrayDark.surfaceContainer,
        primary = BlueGreenGrayDark.surfaceContainerLow,
        tertiary = BlueGreenGrayDark.primaryContainer,
    ),
    ColorTheme.BlackWhiteGray to ThemeSwatchColors(
        surface = BlackWhiteGrayDark.surfaceContainer,
        primary = BlackWhiteGrayDark.surfaceContainerLow,
        tertiary = BlackWhiteGrayDark.primaryContainer,
    ),
)

// —— Vibrancy (saturation) adjustment ————————————————————

private fun applyVibrancy(scheme: ColorScheme, vibrancy: Float): ColorScheme {
    val factor = (vibrancy / 0.2f).coerceIn(0f, 5f)
    fun adj(color: Color): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (color.red * 255).toInt().coerceIn(0, 255),
            (color.green * 255).toInt().coerceIn(0, 255),
            (color.blue * 255).toInt().coerceIn(0, 255),
            hsv,
        )
        hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
        val rgb = android.graphics.Color.HSVToColor((color.alpha * 255).toInt(), hsv)
        return Color(rgb)
    }
    return scheme.copy(
        background = adj(scheme.background),
        surface = adj(scheme.surface),
        surfaceVariant = adj(scheme.surfaceVariant),
        surfaceContainer = adj(scheme.surfaceContainer),
        surfaceContainerLow = adj(scheme.surfaceContainerLow),
        primary = adj(scheme.primary),
        primaryContainer = adj(scheme.primaryContainer),
        secondary = adj(scheme.secondary),
        secondaryContainer = adj(scheme.secondaryContainer),
        tertiary = adj(scheme.tertiary),
        tertiaryContainer = adj(scheme.tertiaryContainer),
        error = adj(scheme.error),
        errorContainer = adj(scheme.errorContainer),
    )
}

private fun themeColorScheme(
    theme: ColorTheme,
    darkTheme: Boolean,
): ColorScheme {
    val swatch = themeSwatches[theme] ?: themeSwatches.getValue(ColorTheme.Default)
    val appChrome = swatch.surface
    val pageBackground = darken(swatch.surface, 0.22f)
    val cardSurface = swatch.primary
    val brightCapsule = swatch.tertiary
    val darkCapsule = darken(swatch.tertiary, 0.42f)
    val onDark = Color(0xFFF7F0E6)
    val onBrightCapsule = readableOn(brightCapsule)
    return when (theme) {
    ColorTheme.Default -> if (darkTheme) DefaultDark else DefaultDark
    ColorTheme.RedYellowPink -> if (darkTheme) RedYellowPinkDark else RedYellowPinkDark
    ColorTheme.YellowGreenGray -> if (darkTheme) YellowGreenGrayDark else YellowGreenGrayDark
    ColorTheme.BlueGreenGray -> if (darkTheme) BlueGreenGrayDark else BlueGreenGrayDark
    ColorTheme.BlackWhiteGray -> if (darkTheme) BlackWhiteGrayDark else BlackWhiteGrayDark
    }.copy(
        background = pageBackground,
        surface = cardSurface,
        surfaceContainer = appChrome,
        surfaceContainerLow = cardSurface,
        surfaceVariant = darkCapsule,
        primary = darkCapsule,
        primaryContainer = darkCapsule,
        tertiary = darkCapsule,
        tertiaryContainer = darkCapsule,
        secondary = brightCapsule,
        secondaryContainer = brightCapsule,
        onBackground = onDark,
        onSurface = onDark,
        onSurfaceVariant = onDark,
        onPrimary = onDark,
        onPrimaryContainer = onDark,
        onTertiary = onDark,
        onTertiaryContainer = onDark,
        onSecondary = onBrightCapsule,
        onSecondaryContainer = onBrightCapsule,
    )
}

private fun darken(color: Color, amount: Float): Color = Color(
    red = (color.red * (1f - amount)).coerceIn(0f, 1f),
    green = (color.green * (1f - amount)).coerceIn(0f, 1f),
    blue = (color.blue * (1f - amount)).coerceIn(0f, 1f),
    alpha = color.alpha,
)

private fun readableOn(color: Color): Color {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (luminance > 0.55f) Color(0xFF17130F) else Color(0xFFF7F0E6)
}

@Composable
fun CarHostTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean,
    colorTheme: ColorTheme = ColorTheme.Default,
    contrastLevel: Float = 0.2f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val baseScheme = themeColorScheme(theme = colorTheme, darkTheme = useDarkTheme)
    val colorScheme = applyVibrancy(baseScheme, contrastLevel)

    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

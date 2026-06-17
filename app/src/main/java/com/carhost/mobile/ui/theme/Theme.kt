package com.carhost.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.carhost.mobile.data.model.ColorTheme

// —— Morandi palette: muted, low-saturation, harmonious tones ————
// Each theme defines 3 key colors: surface (card), primary (button), tertiary (nav capsule)

// ========== 默认 灰蓝紫 ==========
private val DefaultLight = lightColorScheme(
    surface = Color(0xFFFFF8F0),
    primary = Color(0xFF7B73B0),
    tertiary = Color(0xFF9E92C4),
    secondary = Color(0xFF8E8A9E),
    background = Color(0xFFF7F3EC),
    surfaceVariant = Color(0xFFECE6DC),
    onSurface = Color(0xFF1E1B16),
    onPrimary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B16),
    onSurfaceVariant = Color(0xFF4D473E),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val DefaultDark = darkColorScheme(
    surface = Color(0xFF1D1A16),
    primary = Color(0xFF9A93CE),
    tertiary = Color(0xFFB8AED4),
    secondary = Color(0xFF8E8A9E),
    background = Color(0xFF15120E),
    surfaceVariant = Color(0xFF36322C),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF2A2550),
    onTertiary = Color(0xFF2E2550),
    onSecondary = Color(0xFF2E2A3E),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFCAC2B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// ========== 红黄粉 ==========
private val RedYellowPinkLight = lightColorScheme(
    surface = Color(0xFFFDF0EC),
    primary = Color(0xFFD4A898),
    tertiary = Color(0xFFD4B0B8),
    secondary = Color(0xFFC4A098),
    background = Color(0xFFF8F0EC),
    surfaceVariant = Color(0xFFF0E2DC),
    onSurface = Color(0xFF1E1B16),
    onPrimary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B16),
    onSurfaceVariant = Color(0xFF4D403C),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val RedYellowPinkDark = darkColorScheme(
    surface = Color(0xFF2D2222),
    primary = Color(0xFFD4A898),
    tertiary = Color(0xFFD4B0B8),
    secondary = Color(0xFFB89890),
    background = Color(0xFF181212),
    surfaceVariant = Color(0xFF3D302E),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF3A2018),
    onTertiary = Color(0xFF3A2028),
    onSecondary = Color(0xFF2E1E1A),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFCAB8B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// ========== 黄绿灰 ==========
private val YellowGreenGrayLight = lightColorScheme(
    surface = Color(0xFFF5F2E8),
    primary = Color(0xFFA8BC90),
    tertiary = Color(0xFFA8AEA8),
    secondary = Color(0xFFB8BC9E),
    background = Color(0xFFF0F0E8),
    surfaceVariant = Color(0xFFE8E4D8),
    onSurface = Color(0xFF1E1B16),
    onPrimary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B16),
    onSurfaceVariant = Color(0xFF4D4A3E),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val YellowGreenGrayDark = darkColorScheme(
    surface = Color(0xFF242822),
    primary = Color(0xFFA8BC90),
    tertiary = Color(0xFFA8AEA8),
    secondary = Color(0xFF8E9E82),
    background = Color(0xFF181A14),
    surfaceVariant = Color(0xFF343830),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF1E3018),
    onTertiary = Color(0xFF1E2A1E),
    onSecondary = Color(0xFF1E2A1A),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFC4C4B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// ========== 蓝绿灰 ==========
private val BlueGreenGrayLight = lightColorScheme(
    surface = Color(0xFFEEF2F0),
    primary = Color(0xFF80B8C0),
    tertiary = Color(0xFFA8B4B8),
    secondary = Color(0xFF90A8B0),
    background = Color(0xFFE8EEEC),
    surfaceVariant = Color(0xFFDCE4E0),
    onSurface = Color(0xFF1E1B16),
    onPrimary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B16),
    onSurfaceVariant = Color(0xFF3D4A48),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val BlueGreenGrayDark = darkColorScheme(
    surface = Color(0xFF22282A),
    primary = Color(0xFF80B8C0),
    tertiary = Color(0xFFA8B4B8),
    secondary = Color(0xFF7098A0),
    background = Color(0xFF161A1C),
    surfaceVariant = Color(0xFF32383A),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF10303A),
    onTertiary = Color(0xFF1A2E3A),
    onSecondary = Color(0xFF142A32),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFBCC4C4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// ========== 黑白灰 ==========
private val BlackWhiteGrayLight = lightColorScheme(
    surface = Color(0xFFF8F8F8),
    primary = Color(0xFFB8B8B8),
    tertiary = Color(0xFF989898),
    secondary = Color(0xFFA8A8A8),
    background = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurface = Color(0xFF1E1B16),
    onPrimary = Color(0xFF2E2E2E),
    onTertiary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1B16),
    onSurfaceVariant = Color(0xFF4A4848),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val BlackWhiteGrayDark = darkColorScheme(
    surface = Color(0xFF1A1A1A),
    primary = Color(0xFFC8C8C8),
    tertiary = Color(0xFF989898),
    secondary = Color(0xFFA8A8A8),
    background = Color(0xFF121212),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurface = Color(0xFFEAE2D7),
    onPrimary = Color(0xFF2E2E2E),
    onTertiary = Color(0xFF2E2E2E),
    onSecondary = Color(0xFF2E2E2E),
    onBackground = Color(0xFFEAE2D7),
    onSurfaceVariant = Color(0xFFC4C4C4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// —— Swatch colors for the picker UI (the 3 Morandi colors per theme) ————
data class ThemeSwatchColors(
    val surface: Color,
    val primary: Color,
    val tertiary: Color,
)

val themeSwatches = mapOf(
    ColorTheme.Default to ThemeSwatchColors(
        surface = Color(0xFF1D1A16),
        primary = Color(0xFF9A93CE),
        tertiary = Color(0xFFB8AED4),
    ),
    ColorTheme.RedYellowPink to ThemeSwatchColors(
        surface = Color(0xFF2D2222),
        primary = Color(0xFFD4A898),
        tertiary = Color(0xFFD4B0B8),
    ),
    ColorTheme.YellowGreenGray to ThemeSwatchColors(
        surface = Color(0xFF242822),
        primary = Color(0xFFA8BC90),
        tertiary = Color(0xFFA8AEA8),
    ),
    ColorTheme.BlueGreenGray to ThemeSwatchColors(
        surface = Color(0xFF22282A),
        primary = Color(0xFF80B8C0),
        tertiary = Color(0xFFA8B4B8),
    ),
    ColorTheme.BlackWhiteGray to ThemeSwatchColors(
        surface = Color(0xFF1A1A1A),
        primary = Color(0xFFC8C8C8),
        tertiary = Color(0xFF989898),
    ),
)

// —— Contrast adjustment ————————————————————

private fun applyContrast(scheme: ColorScheme, contrast: Float): ColorScheme {
    if (contrast == 0.5f) return scheme
    val bg = scheme.background
    val highSurface = Color(
        red = (bg.red + 0.18f).coerceIn(0f, 1f),
        green = (bg.green + 0.12f).coerceIn(0f, 1f),
        blue = (bg.blue + 0.12f).coerceIn(0f, 1f),
        alpha = 1f,
    )
    val surf = when {
        contrast < 0.5f -> lerpColor(bg, scheme.surface, contrast * 2f)
        else -> lerpColor(scheme.surface, highSurface, (contrast - 0.5f) * 2f)
    }
    return scheme.copy(surface = surf)
}

private fun lerpColor(a: Color, b: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f,
    )
}

private fun themeColorScheme(
    theme: ColorTheme,
    darkTheme: Boolean,
): ColorScheme = when (theme) {
    ColorTheme.Default -> if (darkTheme) DefaultDark else DefaultLight
    ColorTheme.RedYellowPink -> if (darkTheme) RedYellowPinkDark else RedYellowPinkLight
    ColorTheme.YellowGreenGray -> if (darkTheme) YellowGreenGrayDark else YellowGreenGrayLight
    ColorTheme.BlueGreenGray -> if (darkTheme) BlueGreenGrayDark else BlueGreenGrayLight
    ColorTheme.BlackWhiteGray -> if (darkTheme) BlackWhiteGrayDark else BlackWhiteGrayLight
}

@Composable
fun CarHostTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean,
    colorTheme: ColorTheme = ColorTheme.Default,
    contrastLevel: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val baseScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> themeColorScheme(theme = colorTheme, darkTheme = useDarkTheme)
    }
    val colorScheme = applyContrast(baseScheme, contrastLevel)

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

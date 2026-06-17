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

private val WarmLightScheme = lightColorScheme(
    primary = Color(0xFF7C4D0B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF50624C),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF006782),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F3EC),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFECE0CF),
    onSurfaceVariant = Color(0xFF4D4639),
    error = Color(0xFF9F2D22),
    onError = Color(0xFFFFFFFF),
)

private val WarmDarkScheme = darkColorScheme(
    primary = Color(0xFFF2BC6C),
    onPrimary = Color(0xFF452B00),
    secondary = Color(0xFFB8CCB2),
    onSecondary = Color(0xFF243424),
    tertiary = Color(0xFF8FD0E8),
    onTertiary = Color(0xFF003545),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1D1A16),
    onSurface = Color(0xFFEAE2D7),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFD0C4B4),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF5F140D),
)

// —— Color themes ——————————————————————————

private val RedYellowPinkLight = lightColorScheme(
    primary = Color(0xFFB33A3A),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFD4A017),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFD4739A),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFDF6F0),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5E0DC),
    onSurfaceVariant = Color(0xFF534341),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val RedYellowPinkDark = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    onPrimary = Color(0xFF690020),
    secondary = Color(0xFFFDD663),
    onSecondary = Color(0xFF3E2D00),
    tertiary = Color(0xFFFFB0C8),
    onTertiary = Color(0xFF5E113A),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1D1A16),
    onSurface = Color(0xFFEAE2D7),
    surfaceVariant = Color(0xFF534341),
    onSurfaceVariant = Color(0xFFD8C2BC),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF601410),
)

private val YellowGreenGrayLight = lightColorScheme(
    primary = Color(0xFF4E7A3E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFA8B56A),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7A8B9E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8FBF4),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE2ECD6),
    onSurfaceVariant = Color(0xFF43483E),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val YellowGreenGrayDark = darkColorScheme(
    primary = Color(0xFFA8D88C),
    onPrimary = Color(0xFF1D3A12),
    secondary = Color(0xFFCED992),
    onSecondary = Color(0xFF2E331B),
    tertiary = Color(0xFFB0C4D4),
    onTertiary = Color(0xFF1B2D3C),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1D1A16),
    onSurface = Color(0xFFEAE2D7),
    surfaceVariant = Color(0xFF43483E),
    onSurfaceVariant = Color(0xFFC4C8B8),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF601410),
)

private val BlueGreenGrayLight = lightColorScheme(
    primary = Color(0xFF2E7D6B),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF3A7CA5),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7E8B9E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF2F9FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFD8EAE8),
    onSurfaceVariant = Color(0xFF3F4948),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val BlueGreenGrayDark = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF003B36),
    secondary = Color(0xFF81D4FA),
    onSecondary = Color(0xFF003549),
    tertiary = Color(0xFFB0BEC5),
    onTertiary = Color(0xFF1D2F3A),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1D1A16),
    onSurface = Color(0xFFEAE2D7),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFC0CBC8),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF601410),
)

private val BlackWhiteGrayLight = lightColorScheme(
    primary = Color(0xFF505050),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6F6F6F),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF8E8E8E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBFBFB),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF494848),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

private val BlackWhiteGrayDark = darkColorScheme(
    primary = Color(0xFFD0D0D0),
    onPrimary = Color(0xFF2D2D2D),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF2D2D2D),
    tertiary = Color(0xFF929292),
    onTertiary = Color(0xFF1C1B1F),
    background = Color(0xFF15120E),
    onBackground = Color(0xFFEAE2D7),
    surface = Color(0xFF1D1A16),
    onSurface = Color(0xFFEAE2D7),
    surfaceVariant = Color(0xFF444444),
    onSurfaceVariant = Color(0xFFCAC4C0),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF601410),
)

// —— Contrast adjustment ————————————————————

private fun applyContrast(scheme: ColorScheme, contrast: Float): ColorScheme {
    if (contrast == 0.5f) return scheme
    val bg = scheme.background
    val maxSurface = Color(
        red = (bg.red + 0.18f).coerceIn(0f, 1f),
        green = (bg.green + 0.12f).coerceIn(0f, 1f),
        blue = (bg.blue + 0.12f).coerceIn(0f, 1f),
        alpha = 1f,
    )
    val surf = when {
        contrast < 0.5f -> lerpColor(scheme.background, scheme.surface, contrast * 2f)
        else -> lerpColor(scheme.surface, maxSurface, (contrast - 0.5f) * 2f)
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
    ColorTheme.Default -> if (darkTheme) WarmDarkScheme else WarmLightScheme
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

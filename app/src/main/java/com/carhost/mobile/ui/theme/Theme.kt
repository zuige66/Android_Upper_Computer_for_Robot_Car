package com.carhost.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

@Composable
fun CarHostTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> WarmDarkScheme
        else -> WarmLightScheme
    }

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

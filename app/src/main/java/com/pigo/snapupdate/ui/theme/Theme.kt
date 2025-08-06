package com.pigo.snapupdate.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanColorScheme = darkColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryVariant,
    onPrimaryContainer = CleanOnPrimary,
    secondary = CleanSecondary,
    onSecondary = CleanOnSecondary,
    secondaryContainer = CleanSecondaryVariant,
    onSecondaryContainer = CleanOnSecondary,
    tertiary = CleanSecondary,
    onTertiary = CleanOnSecondary,
    background = CleanBackground,
    onBackground = CleanOnBackground,
    surface = CleanSurface,
    onSurface = CleanOnSurface,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanOnSurfaceVariant,
    error = CleanError,
    onError = CleanOnPrimary,
    errorContainer = CleanError,
    onErrorContainer = CleanOnPrimary,
    inverseSurface = CleanInverseSurface,
    inverseOnSurface = CleanOnBackground
)

private val LightColorScheme = lightColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryVariant,
    onPrimaryContainer = CleanOnPrimary,
    secondary = CleanSecondary,
    onSecondary = CleanOnSecondary,
    secondaryContainer = CleanSecondaryVariant,
    onSecondaryContainer = CleanOnSecondary,
    tertiary = CleanSecondary,
    onTertiary = CleanOnSecondary,
    background = CleanBackground,
    onBackground = CleanOnBackground,
    surface = CleanSurface,
    onSurface = CleanOnSurface,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanOnSurfaceVariant,
    error = CleanError,
    onError = CleanOnPrimary,
    errorContainer = CleanError,
    onErrorContainer = CleanOnPrimary,
    inverseSurface = CleanInverseSurface,
    inverseOnSurface = CleanOnBackground
)

@Composable
fun SnapUpdateTheme(
    darkTheme: Boolean = true, // Force clean dark theme
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color for consistent clean theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CleanColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
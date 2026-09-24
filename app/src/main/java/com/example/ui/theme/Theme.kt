package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NovaPrimaryLight,
    onPrimary = NovaOnPrimaryLight,
    primaryContainer = NovaPrimaryContainerLight,
    onPrimaryContainer = NovaOnPrimaryContainerLight,
    secondary = NovaSecondaryLight,
    onSecondary = NovaOnSecondaryLight,
    secondaryContainer = NovaSecondaryContainerLight,
    onSecondaryContainer = NovaOnSecondaryContainerLight,
    tertiary = NovaTertiaryLight,
    onTertiary = NovaOnTertiaryLight,
    tertiaryContainer = NovaTertiaryContainerLight,
    onTertiaryContainer = NovaOnTertiaryContainerLight,
    background = NovaBackgroundLight,
    onBackground = NovaOnBackgroundLight,
    surface = NovaSurfaceLight,
    onSurface = NovaOnSurfaceLight,
    surfaceVariant = NovaSurfaceVariantLight,
    onSurfaceVariant = NovaOnSurfaceVariantLight,
    outline = NovaOutlineLight,
    error = NovaError,
    onError = NovaOnError,
    errorContainer = NovaErrorContainer,
    onErrorContainer = NovaError
)

private val DarkColorScheme = darkColorScheme(
    primary = NovaPrimaryDark,
    onPrimary = NovaOnPrimaryDark,
    primaryContainer = NovaPrimaryContainerDark,
    onPrimaryContainer = NovaOnPrimaryContainerDark,
    secondary = NovaSecondaryDark,
    onSecondary = NovaOnSecondaryDark,
    secondaryContainer = NovaSecondaryContainerDark,
    onSecondaryContainer = NovaOnSecondaryContainerDark,
    tertiary = NovaTertiaryDark,
    onTertiary = NovaOnTertiaryDark,
    tertiaryContainer = NovaTertiaryContainerDark,
    onTertiaryContainer = NovaOnTertiaryContainerDark,
    background = NovaBackgroundDark,
    onBackground = NovaOnBackgroundDark,
    surface = NovaSurfaceDark,
    onSurface = NovaOnSurfaceDark,
    surfaceVariant = NovaSurfaceVariantDark,
    onSurfaceVariant = NovaOnSurfaceVariantDark,
    outline = NovaOutlineDark,
    error = NovaError,
    onError = NovaOnError,
    errorContainer = NovaErrorContainer,
    onErrorContainer = NovaError
)

@Composable
fun PDFNovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

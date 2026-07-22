package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = GreenPrimaryContainer,
    secondary = AmberSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = TealTertiaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishOnPrimaryContainer,
    secondary = PolishPrimary,
    secondaryContainer = PolishSurfaceContainer,
    onSecondaryContainer = PolishTextPrimary,
    tertiary = PolishPrimary,
    tertiaryContainer = PolishPrimaryContainer,
    background = PolishBackground,
    onBackground = PolishTextPrimary,
    surface = Color.White,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishSurfaceContainer,
    onSurfaceVariant = PolishTextSecondary,
    outline = PolishBorder
)

@Composable
fun VetRecordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our vibrant brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


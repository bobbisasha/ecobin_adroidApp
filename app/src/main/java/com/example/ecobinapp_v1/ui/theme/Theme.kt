package com.example.ecobinapp_v1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = EcoGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = EcoGreenSoft,
    onPrimaryContainer = EcoGreenDark,
    secondary = IoTBlue,
    onSecondary = SurfaceWhite,
    secondaryContainer = IoTBlueSoft,
    onSecondaryContainer = IoTBlueDark,
    tertiary = IoTBlue,
    background = SurfaceCanvas,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = ChipBg,
    onSurfaceVariant = Muted,
    outline = Hairline,
    outlineVariant = Hairline,
    error = StatusCritical,
    onError = SurfaceWhite
)

private val DarkColors = darkColorScheme(
    primary = EcoGreen,
    onPrimary = InkDark,
    primaryContainer = EcoGreenDark,
    onPrimaryContainer = EcoGreenSoft,
    secondary = IoTBlue,
    onSecondary = InkDark,
    secondaryContainer = IoTBlueDark,
    onSecondaryContainer = IoTBlueSoft,
    tertiary = IoTBlue,
    background = InkDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = HairlineDark,
    onSurfaceVariant = MutedDark,
    outline = HairlineDark,
    outlineVariant = HairlineDark,
    error = StatusCritical,
    onError = InkDark
)

@Composable
fun EcobinApp_v1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color intentionally off so the ecobin brand palette is always used.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

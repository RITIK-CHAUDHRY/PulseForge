
package com.pulseforge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = CyberPurple,
    primaryVariant = Purple700,
    secondary = NeonBlue,
    background = DeepSpace,
    surface = DeepSpace,
    onPrimary = ElectricGreen,
    onSecondary = ElectricGreen,
    onBackground = NeonBlue,
    onSurface = NeonBlue,
)

private val LightColorPalette = lightColors(
    primary = CyberPurple,
    primaryVariant = Purple700,
    secondary = NeonBlue,
    background = ElectricGreen,
    surface = ElectricGreen,
    onPrimary = DeepSpace,
    onSecondary = DeepSpace,
    onBackground = CyberPurple,
    onSurface = CyberPurple,
)

@Composable
fun PulseForgeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

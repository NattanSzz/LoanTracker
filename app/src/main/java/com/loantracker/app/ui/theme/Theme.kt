package com.loantracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF2E7D32)
val PrimaryGreenDark = Color(0xFF1B5E20)
val AlertRed = Color(0xFFC62828)
val WarningOrange = Color(0xFFEF6C00)
val NeutralBackground = Color(0xFFF7F7F7)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = PrimaryGreenDark,
    background = NeutralBackground,
    surface = Color.White,
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    error = AlertRed
)

@Composable
fun LoanTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

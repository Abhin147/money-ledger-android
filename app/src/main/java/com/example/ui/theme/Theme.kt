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
    primary = CleanBlueLight,
    onPrimary = CleanOnBlueContainer,
    primaryContainer = CleanBlueDark,
    onPrimaryContainer = CleanBlueContainer,
    secondary = Amber400,
    onSecondary = Slate950,
    secondaryContainer = Amber800,
    onSecondaryContainer = Amber100,
    tertiary = Indigo400,
    onTertiary = Color.White,
    tertiaryContainer = Indigo800,
    onTertiaryContainer = Indigo100,
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E2EC),
    surface = Color(0xFF1B1F24),
    onSurface = Color(0xFFE1E2EC),
    surfaceVariant = Color(0xFF242A30),
    onSurfaceVariant = Color(0xFFC4C6D0),
    error = Rose400,
    onError = Slate950,
    errorContainer = Rose800,
    onErrorContainer = Rose100,
    outline = Color(0xFF44474E),
    outlineVariant = Color(0xFF2E3339)
)

private val LightColorScheme = lightColorScheme(
    primary = CleanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = CleanBlueContainer,
    onPrimaryContainer = CleanOnBlueContainer,
    secondary = Amber700,
    onSecondary = Color.White,
    secondaryContainer = Amber50,
    onSecondaryContainer = Amber900,
    tertiary = Indigo700,
    onTertiary = Color.White,
    tertiaryContainer = Indigo50,
    onTertiaryContainer = Indigo900,
    background = CleanBackground,
    onBackground = CleanTextPrimary,
    surface = CleanSurface,
    onSurface = CleanTextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanTextSecondary,
    error = CleanAlertText,
    onError = Color.White,
    errorContainer = CleanAlertBg,
    onErrorContainer = CleanAlertText,
    outline = Color(0xFFC4C6D0),
    outlineVariant = CleanBorderSolid
)

@Composable
fun MoneyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent emerald financial branding
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MoneyLedgerTheme(darkTheme, dynamicColor, content)


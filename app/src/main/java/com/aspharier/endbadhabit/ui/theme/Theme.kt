package com.aspharier.endbadhabit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(val key: String, val label: String, val previewColor: Color) {
    MIDNIGHT("midnight", "Midnight", MidnightPrimary),
    EMBER("ember", "Ember", EmberPrimary),
    OCEAN("ocean", "Ocean", OceanPrimary),
    FOREST("forest", "Forest", ForestPrimary),
    ROSE("rose", "Rose", RosePrimary);

    companion object {
        fun fromKey(key: String): AppTheme {
            return entries.find { it.key == key } ?: MIDNIGHT
        }
    }
}

private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color.White,
    secondary = MidnightAccent,
    onSecondary = Color.Black,
    tertiary = MidnightAccent,
    background = MidnightBackground,
    onBackground = MidnightOnBackground,
    surface = MidnightSurface,
    onSurface = MidnightOnSurface,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MidnightOnSurfaceVariant,
    outline = MidnightOnSurfaceVariant,
)

private val EmberColorScheme = darkColorScheme(
    primary = EmberPrimary,
    onPrimary = Color.White,
    secondary = EmberAccent,
    onSecondary = Color.Black,
    tertiary = EmberAccent,
    background = EmberBackground,
    onBackground = EmberOnBackground,
    surface = EmberSurface,
    onSurface = EmberOnSurface,
    surfaceVariant = EmberSurfaceVariant,
    onSurfaceVariant = EmberOnSurfaceVariant,
    outline = EmberOnSurfaceVariant,
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.White,
    secondary = OceanAccent,
    onSecondary = Color.Black,
    tertiary = OceanAccent,
    background = OceanBackground,
    onBackground = OceanOnBackground,
    surface = OceanSurface,
    onSurface = OceanOnSurface,
    surfaceVariant = OceanSurfaceVariant,
    onSurfaceVariant = OceanOnSurfaceVariant,
    outline = OceanOnSurfaceVariant,
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    secondary = ForestAccent,
    onSecondary = Color.Black,
    tertiary = ForestAccent,
    background = ForestBackground,
    onBackground = ForestOnBackground,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    surfaceVariant = ForestSurfaceVariant,
    onSurfaceVariant = ForestOnSurfaceVariant,
    outline = ForestOnSurfaceVariant,
)

private val RoseColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    secondary = RoseAccent,
    onSecondary = Color.Black,
    tertiary = RoseAccent,
    background = RoseBackground,
    onBackground = RoseOnBackground,
    surface = RoseSurface,
    onSurface = RoseOnSurface,
    surfaceVariant = RoseSurfaceVariant,
    onSurfaceVariant = RoseOnSurfaceVariant,
    outline = RoseOnSurfaceVariant,
)

@Composable
fun EndBadHabitTheme(
    appTheme: AppTheme = AppTheme.MIDNIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.MIDNIGHT -> MidnightColorScheme
        AppTheme.EMBER -> EmberColorScheme
        AppTheme.OCEAN -> OceanColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.ROSE -> RoseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
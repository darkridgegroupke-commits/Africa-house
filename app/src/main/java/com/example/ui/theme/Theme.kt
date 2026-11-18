package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

@Immutable
data class HoaExtendedColors(
    val goldAccent: Color,
    val goldContainer: Color,
    val onGoldContainer: Color,
    val forestHeader: Color,
    val onForestHeader: Color,
    val subtleDivider: Color,
)

val LocalHoaColors = staticCompositionLocalOf {
    HoaExtendedColors(
        goldAccent = SovereignGold,
        goldContainer = SovereignGoldSoft,
        onGoldContainer = InkCharcoalGreen,
        forestHeader = ForestGreenDeep,
        onForestHeader = OffWhiteParchment,
        subtleDivider = ParchmentOutline,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenDeep,
    onPrimary = OffWhiteParchment,
    primaryContainer = ForestGreenSoftTint,
    onPrimaryContainer = ForestGreenDeep,
    secondary = SovereignGold,
    onSecondary = InkCharcoalGreen,
    secondaryContainer = SovereignGoldSoft,
    onSecondaryContainer = SovereignGoldDeep,
    tertiary = SavannaBronze,
    onTertiary = OffWhiteParchment,
    tertiaryContainer = SavannaBronzeSoft,
    onTertiaryContainer = SavannaBronze,
    background = OffWhiteParchment,
    onBackground = InkCharcoalGreen,
    surface = OffWhiteElevated,
    onSurface = InkCharcoalGreen,
    surfaceVariant = OffWhiteSurface,
    onSurfaceVariant = InkMutedGreen,
    surfaceContainerLowest = OffWhiteElevated,
    surfaceContainerLow = OffWhiteSurfaceLow,
    surfaceContainer = OffWhiteSurface,
    surfaceContainerHigh = OffWhiteSurfaceHigh,
    outline = ParchmentOutline,
    outlineVariant = OffWhiteSurfaceHigh,
    error = CrimsonAlertLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = SovereignGoldBright,
    onPrimary = InkCharcoalGreen,
    primaryContainer = ForestGreenRich,
    onPrimaryContainer = OffWhiteParchment,
    secondary = ForestGreenMuted,
    onSecondary = OffWhiteParchment,
    secondaryContainer = DarkEmeraldSurfaceHigh,
    onSecondaryContainer = SovereignGoldSoft,
    tertiary = SavannaBronzeSoft,
    onTertiary = InkCharcoalGreen,
    background = DarkObsidianBg,
    onBackground = DarkOffWhiteText,
    surface = DarkEmeraldSurface,
    onSurface = DarkOffWhiteText,
    surfaceVariant = DarkEmeraldSurfaceLow,
    onSurfaceVariant = DarkMutedSageText,
    surfaceContainerLowest = DarkObsidianBg,
    surfaceContainerLow = DarkEmeraldSurface,
    surfaceContainer = DarkEmeraldSurfaceLow,
    surfaceContainerHigh = DarkEmeraldSurfaceHigh,
    outline = DarkEmeraldOutline,
    outlineVariant = DarkEmeraldSurfaceHigh,
    error = CrimsonAlertDark,
    onError = InkCharcoalGreen,
)

val HoaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HouseOfAfricaTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        HoaExtendedColors(
            goldAccent = SovereignGoldBright,
            goldContainer = DarkEmeraldSurfaceHigh,
            onGoldContainer = SovereignGoldSoft,
            forestHeader = DarkEmeraldSurface,
            onForestHeader = DarkOffWhiteText,
            subtleDivider = DarkEmeraldOutline,
        )
    } else {
        HoaExtendedColors(
            goldAccent = SovereignGold,
            goldContainer = SovereignGoldSoft,
            onGoldContainer = SovereignGoldDeep,
            forestHeader = ForestGreenDeep,
            onForestHeader = OffWhiteParchment,
            subtleDivider = ParchmentOutline,
        )
    }

    CompositionLocalProvider(LocalHoaColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HoaShapes,
            content = content,
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    HouseOfAfricaTheme(
        themePreference = if (darkTheme) ThemePreference.DARK else ThemePreference.LIGHT,
        content = content,
    )
}

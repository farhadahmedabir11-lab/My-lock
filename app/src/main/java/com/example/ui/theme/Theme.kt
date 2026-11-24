package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VaultDarkColorScheme = darkColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = OnCobaltContainer,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = VaultSurfaceHighest,
    onSecondaryContainer = SkyBlueLight,
    tertiary = AmberAccent,
    onTertiary = VaultBackground,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberAccent,
    background = VaultBackground,
    onBackground = Color.White,
    surface = VaultSurface,
    onSurface = Color.White,
    surfaceVariant = VaultSurfaceElevated,
    onSurfaceVariant = SlateLight,
    outline = VaultOutline,
    outlineVariant = VaultOutlineSubtle,
    error = CoralError,
    onError = VaultBackground,
    errorContainer = CoralContainer,
    onErrorContainer = CoralError
)

private val VaultShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyLockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaultDarkColorScheme,
        typography = MyLockTypography,
        shapes = VaultShapes,
        content = content
    )
}

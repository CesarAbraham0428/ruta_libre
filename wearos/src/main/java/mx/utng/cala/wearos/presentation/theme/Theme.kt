package mx.utng.cala.wearos.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme

private val WearColorScheme = ColorScheme(
    primary = Primary,
    onPrimary = Color.Black,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = Color.Black,
    secondaryContainer = Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = Secondary,
    tertiary = Tertiary,
    onTertiary = Color.Black,
    tertiaryContainer = Tertiary.copy(alpha = 0.2f),
    onTertiaryContainer = Tertiary,
    background = Color.Black,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurface,
    surfaceContainer = Surface,
    surfaceContainerHigh = Surface.copy(alpha = 0.8f),
    surfaceContainerLow = Color.Black,
    error = Error,
    onError = Color.White,
    outline = Color.Gray,
    outlineVariant = Color.DarkGray
)

@Composable
fun RutaLibreTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WearColorScheme, content = content)
}

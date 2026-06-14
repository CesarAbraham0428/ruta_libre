package mx.utng.cala.wearos.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme

private val WearColorScheme = ColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Background,
    surfaceContainer = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error
)

@Composable
fun RutaLibreTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WearColorScheme, content = content)
}

package mx.utng.cala.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun RutaLibreTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Primary,
        secondary = Secondary,
        tertiary = Tertiary,
        background = Background,
        surface = Surface,
        error = Error,
        onPrimary = OnBackground,
        onSecondary = OnBackground,
        onTertiary = OnBackground,
        onBackground = OnBackground,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        outline = Outline
    )
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

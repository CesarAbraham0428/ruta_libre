package mx.utng.cala.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import mx.utng.cala.tv.ui.navigation.TvRoutes
import mx.utng.cala.tv.ui.theme.Primary
import mx.utng.cala.tv.ui.theme.PrimaryContainer
import mx.utng.cala.tv.ui.theme.Surface as ColorSuperficie
import mx.utng.cala.tv.ui.theme.SurfaceVariant

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BarraLateralTv(
    navController: NavController,
    rutaSeleccionada: String
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(ColorSuperficie)
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Logo de Ruta Libre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = "Logo",
                tint = Primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Ruta Libre",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Elementos de menú
        ElementoMenuLateral(
            texto = "Dashboard",
            icono = Icons.Default.BarChart,
            seleccionado = rutaSeleccionada == TvRoutes.DASHBOARD,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.DASHBOARD) {
                    navController.navigate(TvRoutes.DASHBOARD) {
                        popUpTo(TvRoutes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElementoMenuLateral(
            texto = "Grupos",
            icono = Icons.Default.Group,
            seleccionado = rutaSeleccionada == TvRoutes.GRUPOS,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.GRUPOS) {
                    navController.navigate(TvRoutes.GRUPOS) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElementoMenuLateral(
            texto = "Contenido",
            icono = Icons.Default.PlayArrow,
            seleccionado = rutaSeleccionada == TvRoutes.VIDEOS,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.VIDEOS) {
                    navController.navigate(TvRoutes.VIDEOS) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ElementoMenuLateral(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    alSeleccionar: () -> Unit
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = alSeleccionar,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (seleccionado) PrimaryContainer else Color.Transparent,
            focusedContainerColor = SurfaceVariant,
            pressedContainerColor = PrimaryContainer
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (seleccionado || tieneFoco) Primary else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = if (seleccionado || tieneFoco) Color.White else Color.Gray
            )
        }
    }
}

package mx.utng.cala.tv.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
/** Renderiza la barra lateral colapsable y sus opciones de navegacion. */
fun BarraLateralTv(
    navController: NavController,
    rutaSeleccionada: String,
    onCerrarSesion: () -> Unit = {}
) {
    // Detectar si la barra lateral tiene foco en alguno de sus elementos
    var tieneFocoMenu by remember { mutableStateOf(false) }

    // Animación suave del ancho de la barra lateral
    val anchoBarra by animateDpAsState(
        targetValue = if (tieneFocoMenu) 260.dp else 72.dp,
        label = "anchoBarraLateralAnimado"
    )

    // Reserva siempre el ancho compacto. Al recibir foco, el menú se dibuja por
    // encima del contenido y ya no comprime las tarjetas del dashboard.
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .zIndex(10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .width(anchoBarra)
                .background(ColorSuperficie)
                .onFocusChanged { estadoFoco ->
                    tieneFocoMenu = estadoFoco.hasFocus
                }
                .padding(top = 32.dp, start = 12.dp, end = 12.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
        // Logo de Ruta Libre colapsable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = if (tieneFocoMenu) Arrangement.Start else Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(start = if (tieneFocoMenu) 8.dp else 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "Logo",
                    tint = Primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            AnimatedVisibility(
                visible = tieneFocoMenu,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruta Libre",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Elementos de menú
        ElementoMenuLateral(
            texto = "Dashboard",
            icono = Icons.Default.BarChart,
            seleccionado = rutaSeleccionada == TvRoutes.DASHBOARD,
            barraExpandida = tieneFocoMenu,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.DASHBOARD) {
                    navController.navigate(TvRoutes.DASHBOARD) {
                        popUpTo(TvRoutes.DASHBOARD) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElementoMenuLateral(
            texto = "Grupos",
            icono = Icons.Default.Group,
            seleccionado = rutaSeleccionada == TvRoutes.GRUPOS,
            barraExpandida = tieneFocoMenu,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.GRUPOS) {
                    navController.navigate(TvRoutes.GRUPOS) {
                        popUpTo(TvRoutes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElementoMenuLateral(
            texto = "Contenido",
            icono = Icons.Default.PlayArrow,
            seleccionado = rutaSeleccionada == TvRoutes.VIDEOS,
            barraExpandida = tieneFocoMenu,
            alSeleccionar = {
                if (rutaSeleccionada != TvRoutes.VIDEOS) {
                    navController.navigate(TvRoutes.VIDEOS) {
                        popUpTo(TvRoutes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        ElementoMenuLateral(
            texto = "Cerrar sesión",
            icono = Icons.Default.Logout,
            seleccionado = false,
            barraExpandida = tieneFocoMenu,
            alSeleccionar = onCerrarSesion
        )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/** Dibuja una opcion enfocables del menu lateral de la TV. */
fun ElementoMenuLateral(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    barraExpandida: Boolean,
    alSeleccionar: () -> Unit
) {
    var tieneFocoElemento by remember { mutableStateOf(false) }

    Surface(
        onClick = alSeleccionar,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .onFocusChanged { estadoFoco -> tieneFocoElemento = estadoFoco.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (seleccionado) PrimaryContainer else Color.Transparent,
            focusedContainerColor = SurfaceVariant,
            pressedContainerColor = PrimaryContainer
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (barraExpandida) Arrangement.Start else Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(start = if (barraExpandida) 12.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (seleccionado || tieneFocoElemento) Primary else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            AnimatedVisibility(
                visible = barraExpandida,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (seleccionado || tieneFocoElemento) Color.White else Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

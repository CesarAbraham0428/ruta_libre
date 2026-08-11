package mx.utng.cala.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.wearos.presentation.components.MetaCompletadaAlerta
import mx.utng.cala.wearos.presentation.screens.InicioScreen
import mx.utng.cala.wearos.presentation.screens.MetricasScreen
import mx.utng.cala.wearos.presentation.viewmodel.WearEntrenamientoViewModel

/** Rutas disponibles dentro de la interfaz de Wear OS. */
object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
}

/** Conecta las pantallas del reloj y muestra las alertas de metas completadas. */
@Composable
fun WearNavGraph(
    navController: NavHostController,
    idUsuario: Int?,
    onCerrarSesion: () -> Unit,
    viewModel: WearEntrenamientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (idUsuario == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Inicia sesión en Ruta Libre desde tu celular")
        }
        return
    }

    NavHost(navController = navController, startDestination = WearRoutes.INICIO) {
        composable(WearRoutes.INICIO) {
            InicioScreen(
                navController = navController,
                onIniciar = { viewModel.iniciar(idUsuario) },
                onCerrarSesion = onCerrarSesion
            )
        }
        composable(WearRoutes.METRICAS) {
            MetricasScreen(
                distancia = uiState.distancia,
                pasos = uiState.pasos,
                calorias = uiState.calorias,
                tiempoSegundos = uiState.tiempo,
                estaActivo = uiState.estaActivo,
                onFinalizar = {
                    viewModel.finalizar(idUsuario) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }

    if (uiState.mostrarMetaCompletada && uiState.metaActual != null) {
        MetaCompletadaAlerta(
            visible = true,
            tipoMeta = uiState.metaActual!!.tipoMeta,
            valorObjetivo = uiState.metaActual!!.valorObjetivo,
            onAceptar = {
                viewModel.aceptarMetaCompletada()
            }
        )
    }
}

package mx.utng.cala.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.wearos.presentation.components.MetaCompletadaAlerta
import mx.utng.cala.wearos.presentation.screens.InicioScreen
import mx.utng.cala.wearos.presentation.screens.MetricasScreen
import mx.utng.cala.wearos.presentation.viewmodel.WearEntrenamientoViewModel

object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
}

@Composable
fun WearNavGraph(
    navController: NavHostController,
    viewModel: WearEntrenamientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = WearRoutes.INICIO) {
        composable(WearRoutes.INICIO) {
            InicioScreen(
                navController = navController,
                onIniciar = { viewModel.iniciar(1) }
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
                    viewModel.finalizar(1) {
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

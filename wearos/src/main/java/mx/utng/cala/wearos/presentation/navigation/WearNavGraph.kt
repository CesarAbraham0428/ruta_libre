package mx.utng.cala.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.wearos.presentation.screens.InicioScreen
import mx.utng.cala.wearos.presentation.screens.MetricasScreen
import mx.utng.cala.wearos.presentation.screens.MetaCompletadaScreen
import mx.utng.cala.wearos.presentation.viewmodel.WearEntrenamientoViewModel

object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
    const val META_COMPLETADA = "meta_completada"
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
                        val currentState = viewModel.uiState.value
                        if (currentState.mostrarMetaCompletada) {
                            navController.navigate(WearRoutes.META_COMPLETADA) {
                                popUpTo(WearRoutes.INICIO)
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
        composable(WearRoutes.META_COMPLETADA) {
            uiState.metaActual?.let { meta ->
                MetaCompletadaScreen(
                    meta = meta,
                    onAceptar = {
                        viewModel.aceptarMetaCompletada()
                        val remaining = uiState.metasCompletadas.size - 1
                        if (remaining <= 0) {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

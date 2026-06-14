package mx.utng.cala.wearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.wearos.presentation.screens.InicioScreen
import mx.utng.cala.wearos.presentation.screens.MetricasScreen

object WearRoutes {
    const val INICIO = "inicio"
    const val METRICAS = "metricas"
    const val LOGROS = "logros"
}

@Composable
fun WearNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = WearRoutes.INICIO) {
        composable(WearRoutes.INICIO) { InicioScreen(navController) }
        composable(WearRoutes.METRICAS) { MetricasScreen(navController) }
        composable(WearRoutes.LOGROS) { MetricasScreen(navController) }
    }
}

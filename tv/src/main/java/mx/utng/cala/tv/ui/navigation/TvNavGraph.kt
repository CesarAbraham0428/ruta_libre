package mx.utng.cala.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.tv.ui.screens.dashboard.DashboardScreen
import mx.utng.cala.tv.ui.screens.grupos.GruposTvScreen
import mx.utng.cala.tv.ui.screens.videos.VideosScreen
import mx.utng.cala.tv.ui.screens.vinculacion.VinculacionTvScreen
import mx.utng.cala.tv.ui.viewmodel.TvPairingViewModel

object TvRoutes {
    const val DASHBOARD = "dashboard"
    const val GRUPOS = "grupos"
    const val VIDEOS = "videos"
    const val VINCULACION = "vinculacion"
}

@Composable
fun TvNavGraph(
    navController: NavHostController,
    remoteLogoutSignal: Int,
    onUsuarioVinculado: (Int?) -> Unit
) {
    val pairingViewModel: TvPairingViewModel = viewModel()
    val pairingState by pairingViewModel.uiState.collectAsState()
    val startDestination = if (pairingState.idUsuario == null) TvRoutes.VINCULACION else TvRoutes.DASHBOARD

    LaunchedEffect(remoteLogoutSignal) {
        if (remoteLogoutSignal > 0) pairingViewModel.cerrarSesionRemota()
    }

    LaunchedEffect(pairingState.idUsuario) {
        onUsuarioVinculado(pairingState.idUsuario)
        if (pairingState.idUsuario != null && navController.currentDestination?.route == TvRoutes.VINCULACION) {
            navController.navigate(TvRoutes.DASHBOARD) { popUpTo(TvRoutes.VINCULACION) { inclusive = true } }
        } else if (pairingState.idUsuario == null && navController.currentDestination?.route != TvRoutes.VINCULACION) {
            navController.navigate(TvRoutes.VINCULACION) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(TvRoutes.VINCULACION) {
            VinculacionTvScreen(pairingState, pairingViewModel::solicitarCodigo)
        }
        composable(TvRoutes.DASHBOARD) {
            pairingState.idUsuario?.let { DashboardScreen(navController, it, pairingViewModel::cerrarSesion) }
        }
        composable(TvRoutes.GRUPOS) {
            pairingState.idUsuario?.let { GruposTvScreen(navController, it, pairingViewModel::cerrarSesion) }
        }
        composable(TvRoutes.VIDEOS) { VideosScreen(navController, pairingViewModel::cerrarSesion) }
    }
}

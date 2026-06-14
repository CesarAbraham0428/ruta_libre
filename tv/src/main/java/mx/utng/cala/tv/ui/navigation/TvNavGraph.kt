package mx.utng.cala.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.tv.ui.screens.dashboard.DashboardScreen
import mx.utng.cala.tv.ui.screens.grupos.GruposTvScreen
import mx.utng.cala.tv.ui.screens.videos.VideosScreen

object TvRoutes {
    const val DASHBOARD = "dashboard"
    const val GRUPOS = "grupos"
    const val VIDEOS = "videos"
}

@Composable
fun TvNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = TvRoutes.DASHBOARD) {
        composable(TvRoutes.DASHBOARD) { DashboardScreen(navController) }
        composable(TvRoutes.GRUPOS) { GruposTvScreen(navController) }
        composable(TvRoutes.VIDEOS) { VideosScreen(navController) }
    }
}

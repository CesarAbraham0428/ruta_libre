package mx.utng.cala.rutalibre.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.utng.cala.rutalibre.ui.screens.auth.LoginScreen
import mx.utng.cala.rutalibre.ui.screens.auth.RegisterScreen
import mx.utng.cala.rutalibre.ui.screens.home.HomeScreen
import mx.utng.cala.rutalibre.ui.screens.entrenamiento.EntrenamientoScreen
import mx.utng.cala.rutalibre.ui.screens.resumen.ResumenScreen
import mx.utng.cala.rutalibre.ui.screens.metas.MetasScreen
import mx.utng.cala.rutalibre.ui.screens.grupos.GruposScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.PerfilScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.ENTRENAMIENTO) { EntrenamientoScreen(navController) }
        composable(Routes.RESUMEN) { ResumenScreen(navController) }
        composable(Routes.METAS) { MetasScreen(navController) }
        composable(Routes.GRUPOS) { GruposScreen(navController) }
        composable(Routes.PERFIL) { PerfilScreen(navController) }
    }
}

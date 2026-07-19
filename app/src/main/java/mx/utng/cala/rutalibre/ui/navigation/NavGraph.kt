package mx.utng.cala.rutalibre.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.utng.cala.rutalibre.ui.screens.auth.LoginScreen
import mx.utng.cala.rutalibre.ui.screens.auth.RegisterScreen
import mx.utng.cala.rutalibre.ui.screens.home.HomeScreen
import mx.utng.cala.rutalibre.ui.screens.entrenamiento.EntrenamientoScreen
import mx.utng.cala.rutalibre.ui.screens.resumen.ResumenScreen
import mx.utng.cala.rutalibre.ui.screens.metas.CrearMetaScreen
import mx.utng.cala.rutalibre.ui.screens.metas.EditarMetaScreen
import mx.utng.cala.rutalibre.ui.screens.metas.MetasScreen
import mx.utng.cala.rutalibre.ui.screens.grupos.GruposScreen
import mx.utng.cala.rutalibre.ui.screens.grupos.DetalleGrupoScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.PerfilScreen
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.GrupoViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val metasViewModel: MetasViewModel = viewModel()
    val grupoViewModel: GrupoViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.ENTRENAMIENTO) { EntrenamientoScreen(navController) }
        composable(Routes.RESUMEN) { ResumenScreen(navController) }
        composable(Routes.METAS) { MetasScreen(navController, metasViewModel, authViewModel) }
        composable(Routes.CREAR_META) { CrearMetaScreen(navController, metasViewModel, authViewModel) }
        composable(
            route = Routes.EDITAR_META,
            arguments = listOf(navArgument("idMeta") { type = NavType.IntType })
        ) { backStackEntry ->
            val idMeta = backStackEntry.arguments?.getInt("idMeta") ?: return@composable
            EditarMetaScreen(navController, metasViewModel, authViewModel, idMeta)
        }
        composable(Routes.GRUPOS) {
            GruposScreen(navController, grupoViewModel, authViewModel)
        }
        composable(
            route = Routes.DETALLE_GRUPO,
            arguments = listOf(
                navArgument("idGrupo") { type = NavType.IntType },
                navArgument("nombreGrupo") { type = NavType.StringType }
            )
        ) { entradaBackStack ->
            val idGrupo = entradaBackStack.arguments?.getInt("idGrupo") ?: return@composable
            val nombreGrupo = entradaBackStack.arguments?.getString("nombreGrupo") ?: ""
            val authState by authViewModel.uiState.collectAsState()
            val idUsuarioActual = authState.idUsuario ?: return@composable
            DetalleGrupoScreen(navController, grupoViewModel, idGrupo, nombreGrupo, idUsuarioActual)
        }
        composable(Routes.PERFIL) { PerfilScreen(navController) }
    }
}

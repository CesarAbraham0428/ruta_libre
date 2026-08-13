package mx.utng.cala.rutalibre.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mx.utng.cala.rutalibre.ui.screens.auth.LoginScreen
import mx.utng.cala.rutalibre.ui.screens.auth.RegisterScreen
import mx.utng.cala.rutalibre.ui.screens.acerca.AcercaDeLaAppScreen
import mx.utng.cala.rutalibre.ui.screens.home.HomeScreen
import mx.utng.cala.rutalibre.ui.screens.entrenamiento.EntrenamientoScreen
import mx.utng.cala.rutalibre.ui.screens.resumen.ResumenScreen
import mx.utng.cala.rutalibre.ui.screens.resumen.HistorialScreen
import mx.utng.cala.rutalibre.ui.screens.metas.CrearMetaScreen
import mx.utng.cala.rutalibre.ui.screens.metas.EditarMetaScreen
import mx.utng.cala.rutalibre.ui.screens.metas.MetasScreen
import mx.utng.cala.rutalibre.ui.screens.grupos.GruposScreen
import mx.utng.cala.rutalibre.ui.screens.grupos.DetalleGrupoScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.PerfilScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.PesoInicialScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.VincularDispositivoScreen
import mx.utng.cala.rutalibre.ui.screens.perfil.DispositivosScreen
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.GrupoViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.PerfilViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.EntrenamientoViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.PesoViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.HistorialViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MqttViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.VincularDispositivoViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.DispositivosViewModel
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import mx.utng.cala.core.data.repository.DispositivoRepository

@Composable
/** Declara las pantallas, argumentos y efectos globales de navegación de la app móvil. */
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val metasViewModel: MetasViewModel = viewModel()
    val grupoViewModel: GrupoViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()
    val entrenamientoViewModel: EntrenamientoViewModel = viewModel()
    val pesoViewModel: PesoViewModel = viewModel()
    val historialViewModel: HistorialViewModel = viewModel()
    val mqttViewModel: MqttViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val mqttState by mqttViewModel.connectionState.collectAsState()
    val context = LocalContext.current
    val dispositivoRepository = remember { DispositivoRepository() }

    LaunchedEffect(authState.idUsuario) {
        authState.idUsuario?.let(mqttViewModel::connect) ?: mqttViewModel.disconnect()
    }

    LaunchedEffect(authState.token) {
        val token = authState.token ?: return@LaunchedEffect
        dispositivoRepository.vincularWear(token).onSuccess { wearIdentity ->
            val request = PutDataMapRequest.create("/ruta-libre/identity").apply {
                dataMap.putInt("idUsuario", wearIdentity.idUsuario)
                dataMap.putString("idDispositivo", wearIdentity.idDispositivo)
                dataMap.putString("token", wearIdentity.token)
                dataMap.putLong("actualizadoEn", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(context).putDataItem(request)
        }
    }

    LaunchedEffect(Unit) {
        mqttViewModel.events.collect { event ->
            if (event.topic.endsWith("/entrenamientos/finalizado")) {
                event.topic.split('/').getOrNull(2)?.toIntOrNull()?.let { idUsuario ->
                    historialViewModel.cargar(idUsuario, forzar = true)
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Routes.HOME) {
            HomeScreen(navController, authViewModel, mqttState.status)
        }
        composable(Routes.PESO_INICIAL) {
            PesoInicialScreen(navController, authViewModel, pesoViewModel)
        }
        composable(Routes.ENTRENAMIENTO) {
            val authState by authViewModel.uiState.collectAsState()
            val idUsuarioActual = authState.idUsuario ?: return@composable
            EntrenamientoScreen(
                navController,
                idUsuarioActual,
                authState.pesoKg ?: 70.0,
                entrenamientoViewModel
            )
        }
        composable(Routes.HISTORIAL) {
            val authState by authViewModel.uiState.collectAsState()
            val idUsuarioActual = authState.idUsuario ?: return@composable
            HistorialScreen(navController, idUsuarioActual, historialViewModel)
        }
        composable(
            route = Routes.RESUMEN,
            arguments = listOf(navArgument("idEntrenamiento") { type = NavType.IntType })
        ) { backStackEntry ->
            val authState by authViewModel.uiState.collectAsState()
            val idUsuarioActual = authState.idUsuario ?: return@composable
            val idEntrenamiento = backStackEntry.arguments?.getInt("idEntrenamiento")
                ?: return@composable
            ResumenScreen(
                navController,
                idUsuarioActual,
                idEntrenamiento,
                historialViewModel
            )
        }
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
        composable(Routes.PERFIL) { PerfilScreen(navController, perfilViewModel, authViewModel) }
        composable(Routes.VINCULAR_DISPOSITIVO) {
            val token = authState.token ?: return@composable
            val vincularDispositivoViewModel: VincularDispositivoViewModel = viewModel()
            VincularDispositivoScreen(navController, token, vincularDispositivoViewModel)
        }
        composable(Routes.DISPOSITIVOS) {
            val dispositivosViewModel: DispositivosViewModel = viewModel()
            DispositivosScreen(navController, authViewModel, dispositivosViewModel)
        }
        composable(Routes.ACERCA_DE_LA_APP) {
            AcercaDeLaAppScreen(navController)
        }
    }
}

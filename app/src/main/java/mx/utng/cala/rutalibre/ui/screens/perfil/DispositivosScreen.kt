package mx.utng.cala.rutalibre.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.Background
import mx.utng.cala.rutalibre.ui.theme.Error
import mx.utng.cala.rutalibre.ui.theme.Surface
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.DispositivosViewModel

/** Lista dispositivos vinculados y ofrece acciones para desvincularlos o cerrar sesiones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispositivosScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: DispositivosViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val token = authState.token ?: return

    LaunchedEffect(token) { viewModel.cargar(token) }
    LaunchedEffect(state.sesionesCerradas) {
        if (state.sesionesCerradas) {
            viewModel.consumirCierreDeSesiones()
            authViewModel.cerrarSesion()
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos vinculados") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Background).padding(padding).padding(20.dp)
        ) {
            if (state.cargando) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            state.error?.let { Text(it, color = Error) }
            if (!state.cargando && state.dispositivos.isEmpty()) {
                Text(
                    "No tienes dispositivos vinculados actualmente.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.dispositivos, key = { it.idDispositivo }) { dispositivo ->
                    Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dispositivo.nombre ?: dispositivo.tipo.uppercase(), fontWeight = FontWeight.Bold)
                                Text(
                                    if (dispositivo.activo) "${dispositivo.tipo.uppercase()} · Activo"
                                    else "${dispositivo.tipo.uppercase()} · Desvinculado",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (dispositivo.activo) {
                                Button(
                                    onClick = { viewModel.desvincular(token, dispositivo.idDispositivo) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                                ) { Text("Desvincular") }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.cerrarTodas(token) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) { Text("CERRAR SESIÓN EN TODOS") }
        }
    }
}

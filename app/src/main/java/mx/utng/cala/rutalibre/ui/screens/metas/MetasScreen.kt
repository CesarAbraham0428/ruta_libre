package mx.utng.cala.rutalibre.ui.screens.metas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    navController: NavController,
    metasViewModel: MetasViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val metasState by metasViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authState.idUsuario?.let { id ->
            metasViewModel.cargarMetas(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Metas") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.CREAR_META) }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Meta")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (metasState.isLoading && metasState.metas.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (metasState.metas.isEmpty()) {
                Text(
                    text = "Aún no tienes metas. ¡Crea una!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(metasState.metas) { meta ->
                        MetaCard(meta = meta)
                    }
                }
            }
        }
    }
}

@Composable
fun MetaCard(meta: MetaResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meta.tipoMeta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = if (meta.terminada) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (meta.terminada) "Terminada" else "En curso",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (meta.valorObjetivo > 0) {
                (meta.valorActual / meta.valorObjetivo).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actual: ${meta.valorActual}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Objetivo: ${meta.valorObjetivo}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

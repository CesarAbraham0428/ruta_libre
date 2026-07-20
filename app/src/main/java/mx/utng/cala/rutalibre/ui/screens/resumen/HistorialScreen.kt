package mx.utng.cala.rutalibre.ui.screens.resumen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.core.data.dto.response.EntrenamientoResponse
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.Background
import mx.utng.cala.rutalibre.ui.theme.Calorias
import mx.utng.cala.rutalibre.ui.theme.Distancia
import mx.utng.cala.rutalibre.ui.theme.OnBackground
import mx.utng.cala.rutalibre.ui.theme.OnSurface
import mx.utng.cala.rutalibre.ui.theme.OnSurfaceVariant
import mx.utng.cala.rutalibre.ui.theme.Pasos
import mx.utng.cala.rutalibre.ui.theme.Primary
import mx.utng.cala.rutalibre.ui.theme.Surface
import mx.utng.cala.rutalibre.ui.theme.Tiempo
import mx.utng.cala.rutalibre.ui.viewmodel.HistorialViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavController,
    idUsuario: Int,
    viewModel: HistorialViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) { viewModel.cargar(idUsuario, forzar = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de actividades", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground,
                    navigationIconContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )

                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.cargar(idUsuario, forzar = true) }) {
                        Text("REINTENTAR")
                    }
                }

                state.entrenamientos.isEmpty() -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = Primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Aún no tienes actividades guardadas", color = OnSurface)
                    Text("Tu próximo entrenamiento aparecerá aquí", color = OnSurfaceVariant)
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(
                        items = state.entrenamientos,
                        key = { it.idEntrenamiento }
                    ) { entrenamiento ->
                        HistorialCard(entrenamiento) {
                            navController.navigate(Routes.resumen(entrenamiento.idEntrenamiento))
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistorialCard(entrenamiento: EntrenamientoResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = formatDate(entrenamiento.fechaInicio),
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Entrenamiento #${entrenamiento.idEntrenamiento}",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalle", tint = Primary)
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMetric(Icons.Default.Route, formatDistance(entrenamiento.distancia), Distancia)
                MiniMetric(Icons.Default.AccessTime, formatDuration(entrenamiento.tiempo), Tiempo)
                MiniMetric(Icons.AutoMirrored.Filled.DirectionsRun, "${entrenamiento.pasos}", Pasos)
                MiniMetric(Icons.Default.LocalFireDepartment, "${entrenamiento.calorias}", Calorias)
            }
        }
    }
}

@Composable
private fun MiniMetric(icon: ImageVector, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color)
        Text(value, color = OnSurface, style = MaterialTheme.typography.labelMedium)
    }
}

internal fun formatDistance(distance: Double): String =
    String.format(Locale.US, "%.2f km", distance)

internal fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

internal fun formatDate(value: String): String = try {
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "MX"))
    Instant.parse(value).atZone(ZoneId.systemDefault()).format(formatter)
} catch (_: Exception) {
    value
}

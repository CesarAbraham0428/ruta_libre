package mx.utng.cala.rutalibre.ui.screens.resumen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    navController: NavController,
    idUsuario: Int,
    idEntrenamiento: Int,
    viewModel: HistorialViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val entrenamiento = state.entrenamientos.firstOrNull {
        it.idEntrenamiento == idEntrenamiento
    }

    LaunchedEffect(idUsuario, idEntrenamiento) {
        if (entrenamiento == null) viewModel.cargar(idUsuario, forzar = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de actividad", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )

                entrenamiento == null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "No se encontró el entrenamiento", color = OnSurface)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.cargar(idUsuario, forzar = true) }) {
                        Text("REINTENTAR")
                    }
                }

                else -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Primary.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("¡Actividad completada!", color = OnBackground, fontWeight = FontWeight.Bold)
                    Text(formatDate(entrenamiento.fechaInicio), color = OnSurfaceVariant)
                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMetric(
                            "Distancia", formatDistance(entrenamiento.distancia),
                            Icons.Default.Route, Distancia, Modifier.weight(1f)
                        )
                        SummaryMetric(
                            "Tiempo", formatDuration(entrenamiento.tiempo),
                            Icons.Default.AccessTime, Tiempo, Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMetric(
                            "Pasos", "${entrenamiento.pasos}",
                            Icons.AutoMirrored.Filled.DirectionsRun, Pasos, Modifier.weight(1f)
                        )
                        SummaryMetric(
                            "Calorías", "${entrenamiento.calorias} kcal",
                            Icons.Default.LocalFireDepartment, Calorias, Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("VOLVER AL INICIO", color = OnSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent)
            Spacer(Modifier.height(8.dp))
            Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text(value, color = OnSurface, fontWeight = FontWeight.Bold)
        }
    }
}

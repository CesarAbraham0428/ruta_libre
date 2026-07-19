package mx.utng.cala.rutalibre.ui.screens.entrenamiento

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientoScreen(navController: NavController) {
    // Datos simulados en español para el entrenamiento completado
    val distanciaSimulada = "5.4"
    val tiempoSimulado = "00:45:20"
    val caloriasSimuladas = "380"
    val ritmoSimulado = "5'25\""
    val pasosSimulados = "6,200"
    val fechaSimulada = "18 de julio de 2026, 08:30 AM"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Resumen del entrenamiento",
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = OnBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Compartir */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Mapa simulado de la ruta
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .border(1.dp, Outline, RoundedCornerShape(20.dp))
                ) {
                    val columnas = 8
                    val filas = 6
                    val anchoPaso = size.width / columnas
                    val altoPaso = size.height / filas
                    for (i in 1 until columnas) {
                        drawLine(
                            color = Outline.copy(alpha = 0.15f),
                            start = Offset(i * anchoPaso, 0f),
                            end = Offset(i * anchoPaso, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 1 until filas) {
                        drawLine(
                            color = Outline.copy(alpha = 0.15f),
                            start = Offset(0f, i * altoPaso),
                            end = Offset(size.width, i * altoPaso),
                            strokeWidth = 1f
                        )
                    }

                    val camino = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.75f)
                        cubicTo(
                            size.width * 0.3f, size.height * 0.2f,
                            size.width * 0.5f, size.height * 0.9f,
                            size.width * 0.65f, size.height * 0.4f
                        )
                        quadraticTo(
                            size.width * 0.75f, size.height * 0.15f,
                            size.width * 0.85f, size.height * 0.35f
                        )
                    }

                    drawPath(
                        path = camino,
                        color = Primary,
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = Offset(size.width * 0.15f, size.height * 0.75f)
                    )
                    drawCircle(
                        color = Primary,
                        radius = 8f,
                        center = Offset(size.width * 0.15f, size.height * 0.75f)
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = Offset(size.width * 0.85f, size.height * 0.35f)
                    )
                    drawCircle(
                        color = Error,
                        radius = 8f,
                        center = Offset(size.width * 0.85f, size.height * 0.35f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fechaSimulada,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Duración: $tiempoSimulado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Sección de estadísticas
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fila 1: Distancia y Tiempo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaEstadistica(
                        titulo = "Distancia",
                        valor = distanciaSimulada,
                        unidad = "km",
                        icono = Icons.Default.Place,
                        colorIcono = Distancia,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        titulo = "Tiempo",
                        valor = tiempoSimulado,
                        unidad = "",
                        icono = Icons.Default.AccessTime,
                        colorIcono = Tiempo,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Calorías y Ritmo medio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaEstadistica(
                        titulo = "Calorías",
                        valor = caloriasSimuladas,
                        unidad = "kcal",
                        icono = Icons.Default.LocalFireDepartment,
                        colorIcono = Calorias,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        titulo = "Ritmo medio",
                        valor = ritmoSimulado,
                        unidad = "min/km",
                        icono = Icons.Default.Speed,
                        colorIcono = Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 3: Pasos (Completo)
                TarjetaEstadistica(
                    titulo = "Pasos",
                    valor = pasosSimulados,
                    unidad = "pasos",
                    icono = Icons.AutoMirrored.Filled.DirectionsRun,
                    colorIcono = Pasos,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Botón de Volver al inicio
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Background
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Volver al inicio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun TarjetaEstadistica(
    titulo: String,
    valor: String,
    unidad: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorIcono,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                if (unidad.isNotEmpty()) {
                    Text(
                        text = unidad,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

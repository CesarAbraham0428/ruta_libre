package mx.utng.cala.wearos.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ScreenScaffold
import kotlinx.coroutines.delay
import mx.utng.cala.wearos.presentation.theme.*
import java.util.Locale

@Composable
fun MetricasScreen(
    distancia: Double,
    pasos: Int,
    calorias: Int,
    tiempoSegundos: Int,
    estaActivo: Boolean,
    onFinalizar: () -> Unit
) {
    var tiempoLocal by remember { mutableIntStateOf(tiempoSegundos) }
    val listState = rememberScalingLazyListState()

    // El cronómetro SIEMPRE corre mientras estaActivo sea true, ignorando tiempoSegundos externo
    LaunchedEffect(estaActivo) {
        if (estaActivo) {
            val baseTime = System.currentTimeMillis() - (tiempoLocal * 1000L)
            while (true) {
                val now = System.currentTimeMillis()
                tiempoLocal = ((now - baseTime) / 1000).toInt()
                delay(500)
            }
        }
    }

    val horas = tiempoLocal / 3600
    val minutos = (tiempoLocal % 3600) / 60
    val segundos = tiempoLocal % 60
    val tiempoFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos)

    ScreenScaffold(scrollState = listState) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 24.dp,
                bottom = 40.dp
            )
        ) {
            item {
                Icon(
                    imageVector = Icons.Filled.DirectionsRun,
                    contentDescription = "Correr",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Ruta Libre",
                        color = OnBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tiempoFormatted,
                        color = OnBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tiempo en actividad",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MetricRow(
                        icon = Icons.Filled.LocationOn,
                        iconColor = MetricDistancia,
                        label = "Distancia",
                        value = String.format(Locale.getDefault(), "%.2f", distancia),
                        unit = "km"
                    )
                    MetricRow(
                        icon = Icons.Filled.DirectionsWalk,
                        iconColor = MetricPasos,
                        label = "Pasos",
                        value = String.format(Locale.getDefault(), "%,d", pasos),
                        unit = ""
                    )
                    MetricRow(
                        icon = Icons.Filled.LocalFireDepartment,
                        iconColor = MetricCalorias,
                        label = "Calorías",
                        value = calorias.toString(),
                        unit = "kcal"
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                Button(
                    onClick = onFinalizar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "FINALIZAR",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = OnBackground,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = OnBackground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(3.dp))
            Text(
                text = unit,
                color = Color.Gray,
                fontSize = 9.sp
            )
        }
    }
}

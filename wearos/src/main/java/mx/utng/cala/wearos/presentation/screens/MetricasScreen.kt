package mx.utng.cala.wearos.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.delay
import mx.utng.cala.wearos.presentation.theme.*

@Composable
fun MetricasScreen(
    distancia: Double,
    pasos: Int,
    calorias: Int,
    tiempoSegundos: Int,
    onFinalizar: () -> Unit
) {
    var tiempoLocal by remember { mutableIntStateOf(tiempoSegundos) }

    LaunchedEffect(tiempoSegundos) {
        tiempoLocal = tiempoSegundos
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tiempoLocal++
        }
    }

    val horas = tiempoLocal / 3600
    val minutos = (tiempoLocal % 3600) / 60
    val segundos = tiempoLocal % 60
    val tiempoFormatted = String.format("%02d:%02d:%02d", horas, minutos, segundos)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsRun,
            contentDescription = "Correr",
            tint = Primary,
            modifier = Modifier.size(32.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ruta Libre",
                color = OnBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = tiempoFormatted,
                color = OnBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tiempo en actividad",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricRow(
                icon = Icons.Filled.LocationOn,
                iconColor = MetricDistancia,
                label = "Distancia",
                value = String.format("%.2f", distancia),
                unit = "km"
            )
            MetricRow(
                icon = Icons.Filled.DirectionsWalk,
                iconColor = MetricPasos,
                label = "Pasos",
                value = String.format("%,d", pasos),
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

        Button(
            onClick = onFinalizar,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Error)
        ) {
            Text(
                text = "FINALIZAR",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = OnBackground,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = OnBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
            Spacer(Modifier.width(2.dp))
            Text(
                text = unit,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

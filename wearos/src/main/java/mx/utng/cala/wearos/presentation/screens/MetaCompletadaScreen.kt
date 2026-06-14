package mx.utng.cala.wearos.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import mx.utng.cala.wearos.presentation.viewmodel.MetaCompletada
import mx.utng.cala.wearos.presentation.theme.*

@Composable
fun MetaCompletadaScreen(
    meta: MetaCompletada,
    onAceptar: () -> Unit
) {
    val (icono, label, unidad) = when (meta.tipoMeta) {
        mx.utng.cala.core.data.model.TipoMeta.DISTANCIA ->
            Triple(Icons.Filled.LocationOn, "Distancia", "km")
        mx.utng.cala.core.data.model.TipoMeta.PASOS ->
            Triple(Icons.Filled.DirectionsWalk, "Pasos", "")
        mx.utng.cala.core.data.model.TipoMeta.CALORIAS ->
            Triple(Icons.Filled.LocalFireDepartment, "Calorías", "kcal")
        mx.utng.cala.core.data.model.TipoMeta.TIEMPO ->
            Triple(Icons.Filled.Timer, "Tiempo", "min")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Trofeo",
            tint = Primary,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "¡Meta completada!",
            color = OnBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = label,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = "${meta.valorObjetivo.toInt()} $unidad",
                    color = OnBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = onAceptar,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(
                text = "ACEPTAR",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

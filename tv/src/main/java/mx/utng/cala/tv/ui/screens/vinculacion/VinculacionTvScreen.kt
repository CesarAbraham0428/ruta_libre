package mx.utng.cala.tv.ui.screens.vinculacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import mx.utng.cala.tv.ui.theme.Background
import mx.utng.cala.tv.ui.viewmodel.TvPairingUiState

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
/** Muestra el codigo y el estado del proceso de vinculacion de la TV. */
fun VinculacionTvScreen(state: TvPairingUiState, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Vincula esta TV", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text(
            "En el celular abre Ruta Libre > Vincular TV y escribe este código:",
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Text(
            state.codigo ?: if (state.cargando) "GENERANDO..." else "------",
            fontSize = 58.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 10.sp,
            color = Color(0xFF78E04F)
        )
        state.error?.let {
            Spacer(Modifier.height(20.dp))
            Text(it, color = Color(0xFFFF6B6B))
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                scale = ButtonDefaults.scale(focusedScale = 1.0f)
            ) { Text("Generar otro código") }
        }
    }
}

package mx.utng.cala.rutalibre.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.Background
import mx.utng.cala.rutalibre.ui.theme.Error
import mx.utng.cala.rutalibre.ui.theme.OnBackground
import mx.utng.cala.rutalibre.ui.theme.OnSurfaceVariant
import mx.utng.cala.rutalibre.ui.theme.Outline
import mx.utng.cala.rutalibre.ui.theme.Primary
import mx.utng.cala.rutalibre.ui.theme.Surface
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.PesoViewModel

/** Solicita el peso inicial necesario para calcular calorías y métricas personales. */
@Composable
fun PesoInicialScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    pesoViewModel: PesoViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val estado by pesoViewModel.estado.collectAsState()
    var pesoTexto by remember { mutableStateOf("") }
    val peso = pesoTexto.replace(',', '.').toDoubleOrNull()
    val pesoValido = peso != null && peso in 20.0..300.0

    LaunchedEffect(estado.guardado) {
        if (estado.guardado && peso != null) {
            authViewModel.actualizarPesoLocal(peso)
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.PESO_INICIAL) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MonitorWeight,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.height(72.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Configura tu peso",
            color = OnBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Lo utilizaremos para estimar las calorías de tus entrenamientos. Podrás cambiarlo después desde tu perfil.",
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = pesoTexto,
            onValueChange = { nuevo ->
                if (nuevo.length <= 6 && nuevo.all { it.isDigit() || it == '.' || it == ',' }) {
                    pesoTexto = nuevo
                }
            },
            label = { Text("Peso") },
            suffix = { Text("kg") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (pesoTexto.isNotEmpty() && !pesoValido) {
            Spacer(Modifier.height(8.dp))
            Text("Ingresa un peso entre 20 y 300 kg", color = Error)
        }
        estado.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val idUsuario = authState.idUsuario
                if (idUsuario != null && peso != null) pesoViewModel.guardarPeso(idUsuario, peso)
            },
            enabled = pesoValido && !estado.guardando,
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color(0xFF050B17)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (estado.guardando) CircularProgressIndicator(strokeWidth = 2.dp)
            else Text("GUARDAR Y CONTINUAR", fontWeight = FontWeight.Bold)
        }
    }
}

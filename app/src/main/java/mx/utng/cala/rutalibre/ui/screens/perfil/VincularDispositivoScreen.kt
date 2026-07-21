package mx.utng.cala.rutalibre.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.theme.Background
import mx.utng.cala.rutalibre.ui.viewmodel.VincularDispositivoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VincularDispositivoScreen(
    navController: NavController,
    token: String,
    viewModel: VincularDispositivoViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var codigo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.reiniciar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular dispositivo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Escribe el código que aparece en Ruta Libre TV",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text("El código dura 10 minutos y sólo puede usarse una vez.")
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { value ->
                    codigo = value.filter(Char::isLetterOrDigit).uppercase().take(6)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Código de TV") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            state.mensaje?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.vincular(token, codigo) },
                enabled = !state.cargando && !state.vinculado,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.cargando) CircularProgressIndicator(modifier = Modifier.height(20.dp))
                else Text(if (state.vinculado) "VINCULADO" else "VINCULAR TV")
            }
        }
    }
}

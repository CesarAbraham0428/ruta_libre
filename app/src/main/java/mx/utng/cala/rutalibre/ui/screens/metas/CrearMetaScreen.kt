package mx.utng.cala.rutalibre.ui.screens.metas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMetaScreen(
    navController: NavController,
    metasViewModel: MetasViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val metasState by metasViewModel.uiState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf(TipoMeta.DISTANCIA) }
    var valorObjetivo by remember { mutableStateOf("") }

    LaunchedEffect(metasState.isMetaCreated) {
        if (metasState.isMetaCreated) {
            metasViewModel.resetMetaCreatedState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Meta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            metasState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedTipo.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de meta") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TipoMeta.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.name) },
                            onClick = {
                                selectedTipo = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = valorObjetivo,
                onValueChange = { valorObjetivo = it },
                label = { Text("Valor objetivo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authState.idUsuario?.let { id ->
                        val valor = valorObjetivo.toDoubleOrNull()
                        if (valor != null && valor > 0) {
                            metasViewModel.crearMeta(id, selectedTipo, valor)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !metasState.isLoading && valorObjetivo.isNotEmpty()
            ) {
                if (metasState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Meta")
                }
            }
        }
    }
}

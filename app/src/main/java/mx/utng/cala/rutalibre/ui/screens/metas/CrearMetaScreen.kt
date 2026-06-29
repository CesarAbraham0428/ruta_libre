package mx.utng.cala.rutalibre.ui.screens.metas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.rutalibre.ui.theme.*
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
    var selectedTipo by remember { mutableStateOf(TipoMeta.PASOS) }
    var valorObjetivo by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    val tipoMetaConfig = remember {
        mapOf(
            TipoMeta.PASOS to TipoMetaInfo(Icons.Default.DirectionsRun, "Pasos", "pasos"),
            TipoMeta.CALORIAS to TipoMetaInfo(Icons.Default.LocalFireDepartment, "Calorías", "kcal"),
            TipoMeta.DISTANCIA to TipoMetaInfo(Icons.Default.Place, "Distancia", "km"),
            TipoMeta.TIEMPO to TipoMetaInfo(Icons.Default.AccessTime, "Tiempo de actividad", "min")
        )
    }

    val currentConfig = tipoMetaConfig[selectedTipo] ?: tipoMetaConfig[TipoMeta.PASOS]!!

    val activeTipos = remember(metasState.metas) {
        metasState.metas.filter { !it.terminada }.map { it.tipoMeta.uppercase() }.toSet()
    }

    val availableTipos = remember(activeTipos) {
        TipoMeta.entries.filter { it.name !in activeTipos }
    }

    LaunchedEffect(Unit) {
        authState.idUsuario?.let { metasViewModel.cargarMetas(it) }
    }

    LaunchedEffect(availableTipos) {
        if (selectedTipo !in availableTipos && availableTipos.isNotEmpty()) {
            selectedTipo = availableTipos.first()
        }
    }

    LaunchedEffect(metasState.isMetaCreated) {
        if (metasState.isMetaCreated) {
            metasViewModel.resetMetaCreatedState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear meta",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    metasState.error?.let { error ->
                        Text(
                            text = error,
                            color = Error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Text(
                        text = "Tipo de meta",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (availableTipos.isEmpty()) {
                        Text(
                            text = "Completa tus metas actuales para poder crear nuevas metas",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = currentConfig.displayName,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = currentConfig.icon,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expandir",
                                        tint = OnSurfaceVariant
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Outline,
                                    unfocusedBorderColor = Outline,
                                    focusedContainerColor = Surface,
                                    unfocusedContainerColor = Surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                containerColor = SurfaceVariant
                            ) {
                                availableTipos.forEach { tipo ->
                                    val config = tipoMetaConfig[tipo] ?: return@forEach
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = config.icon,
                                                    contentDescription = null,
                                                    tint = Primary
                                                )
                                                Text(config.displayName)
                                            }
                                        },
                                        onClick = {
                                            selectedTipo = tipo
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                        if (inputError) {
                        Text(
                            text = "Solo se permiten números",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Meta",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = valorObjetivo,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' }) {
                                valorObjetivo = it
                                inputError = false
                            } else {
                                inputError = true
                            }
                        },
                        isError = inputError,
                        placeholder = { Text("0", color = OnSurfaceVariant) },
                        suffix = {
                            Text(
                                text = currentConfig.unit,
                                color = OnSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Outline,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OnSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        "CANCELAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        authState.idUsuario?.let { id ->
                            val valor = valorObjetivo.toDoubleOrNull()
                            if (valor != null && valor > 0) {
                                metasViewModel.crearMeta(id, selectedTipo, valor)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    enabled = !metasState.isLoading && !inputError && valorObjetivo.isNotEmpty() && availableTipos.isNotEmpty()
                ) {
                    if (metasState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Background
                        )
                    } else {
                        Text(
                            "GUARDAR",
                            color = Background,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private data class TipoMetaInfo(
    val icon: ImageVector,
    val displayName: String,
    val unit: String
)

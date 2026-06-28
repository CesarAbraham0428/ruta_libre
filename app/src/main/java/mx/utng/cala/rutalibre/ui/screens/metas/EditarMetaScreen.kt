package mx.utng.cala.rutalibre.ui.screens.metas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel

private val tipoMetaConfig = mapOf(
    "PASOS" to TipoMetaInfoEdit(Icons.Default.DirectionsRun, "Pasos", Pasos, "pasos"),
    "CALORIAS" to TipoMetaInfoEdit(Icons.Default.LocalFireDepartment, "Calorías", Calorias, "kcal"),
    "DISTANCIA" to TipoMetaInfoEdit(Icons.Default.Place, "Distancia", Distancia, "km"),
    "TIEMPO" to TipoMetaInfoEdit(Icons.Default.AccessTime, "Tiempo de actividad", Tiempo, "min")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMetaScreen(
    navController: NavController,
    metasViewModel: MetasViewModel,
    authViewModel: AuthViewModel,
    idMeta: Int
) {
    val authState by authViewModel.uiState.collectAsState()
    val metasState by metasViewModel.uiState.collectAsState()

    val meta = remember(metasState.metas) {
        metasState.metas.find { it.idMetas == idMeta }
    }

    val config = meta?.let { tipoMetaConfig[it.tipoMeta] }
        ?: tipoMetaConfig["PASOS"]!!

    var nuevoValor by remember(meta) {
        mutableStateOf(meta?.valorObjetivo?.toInt()?.toString() ?: "")
    }

    LaunchedEffect(metasState.isMetaUpdated) {
        if (metasState.isMetaUpdated) {
            metasViewModel.resetMetaUpdatedState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar meta",
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Icon(
                            imageVector = config.icon,
                            contentDescription = null,
                            tint = config.color,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = config.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }

                    Text(
                        text = "Meta actual",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${meta?.valorObjetivo?.toInt() ?: 0} ${config.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Text(
                        text = "Nueva meta",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = nuevoValor,
                        onValueChange = { nuevoValor = it },
                        placeholder = { Text("0", color = OnSurfaceVariant) },
                        suffix = {
                            Text(
                                text = config.unit,
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
                            val valor = nuevoValor.toDoubleOrNull()
                            if (valor != null && valor > 0) {
                                metasViewModel.editarMeta(id, idMeta, valor)
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
                    enabled = !metasState.isLoading && nuevoValor.isNotEmpty()
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

private data class TipoMetaInfoEdit(
    val icon: ImageVector,
    val displayName: String,
    val color: androidx.compose.ui.graphics.Color,
    val unit: String
)

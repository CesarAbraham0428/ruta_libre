package mx.utng.cala.rutalibre.ui.screens.metas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
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
import mx.utng.cala.rutalibre.ui.navigation.Routes
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.AuthViewModel
import mx.utng.cala.rutalibre.ui.viewmodel.MetasViewModel

private val tipoMetaIcons = mapOf(
    "PASOS" to MetaTypeInfo(Icons.Default.DirectionsRun, Pasos, "pasos"),
    "CALORIAS" to MetaTypeInfo(Icons.Default.LocalFireDepartment, Calorias, "kcal"),
    "DISTANCIA" to MetaTypeInfo(Icons.Default.Place, Distancia, "km"),
    "TIEMPO" to MetaTypeInfo(Icons.Default.AccessTime, Tiempo, "min")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    navController: NavController,
    metasViewModel: MetasViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val metasState by metasViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authState.idUsuario?.let { id ->
            metasViewModel.cargarMetas(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Metas",
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
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.CREAR_META) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nueva Meta",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (metasState.isLoading && metasState.metas.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            } else if (metasState.metas.isEmpty()) {
                Text(
                    text = "Aún no tienes metas. ¡Crea una!",
                    modifier = Modifier.align(Alignment.Center),
                    color = OnSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(metasState.metas) { meta ->
                        MetaCard(meta = meta)
                    }
                }
            }
        }
    }
}

@Composable
fun MetaCard(meta: MetaResponse) {
    val typeInfo = tipoMetaIcons[meta.tipoMeta]
        ?: MetaTypeInfo(Icons.Default.DirectionsRun, OnSurfaceVariant, "unidades")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = typeInfo.icon,
                        contentDescription = null,
                        tint = typeInfo.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = meta.tipoMeta.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "${meta.valorObjetivo.toInt()} ${typeInfo.unit}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { /* TODO: Editar meta */ }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = OnSurfaceVariant
                        )
                    }
                    IconButton(onClick = { /* TODO: Eliminar meta */ }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (meta.valorObjetivo > 0) {
                (meta.valorActual / meta.valorObjetivo).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = typeInfo.color,
                trackColor = Surface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actual: ${meta.valorActual.toInt()} ${typeInfo.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "Objetivo: ${meta.valorObjetivo.toInt()} ${typeInfo.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
        }
    }
}

private data class MetaTypeInfo(
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val unit: String
)

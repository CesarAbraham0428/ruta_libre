package mx.utng.cala.rutalibre.ui.screens.grupos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.utng.cala.rutalibre.ui.theme.*
import mx.utng.cala.rutalibre.ui.viewmodel.GrupoViewModel
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGrupoScreen(
    navController: NavController,
    grupoViewModel: GrupoViewModel,
    idGrupo: Int,
    nombreGrupo: String,
    idUsuarioActual: Int
) {
    val estadoUi by grupoViewModel.estadoUi.collectAsState()
    var pestanaSeleccionada by remember { mutableStateOf(0) }

    var mostrarDialogoSalir by remember { mutableStateOf(false) }
    LaunchedEffect(idGrupo) {
        grupoViewModel.cargarDetalleGrupo(idGrupo)
    }

    // Calcular estadísticas totales de los miembros
    val miembros = estadoUi.listaMiembros
    val distanciaTotal = miembros.sumOf { it.distancia }
    val pasosTotales = miembros.sumOf { it.pasos }
    val caloriasTotales = miembros.sumOf { it.calorias }
    val tiempoTotalSegundos = miembros.sumOf { it.tiempo }
    val tiempoTotalMinutos = tiempoTotalSegundos / 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        nombreGrupo,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoSalir = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Salir del grupo",
                            tint = Error
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pestañas (Tabs)
            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = Background,
                contentColor = Primary
            ) {
                Tab(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    text = {
                        Text(
                            "Estadísticas",
                            fontWeight = if (pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Primary,
                    unselectedContentColor = OnSurfaceVariant
                )
                Tab(
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    text = {
                        Text(
                            "Miembros",
                            fontWeight = if (pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Primary,
                    unselectedContentColor = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            estadoUi.mensajeError?.let { error ->
                Text(
                    text = error,
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Contenido de las pestañas
            if (pestanaSeleccionada == 0) {
                // ESTADÍSTICAS Y RANKING
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Tarjetas de Totales Grupales
                    item {
                        Text(
                            text = "Rendimiento Total del Grupo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TarjetaMetricaGrupal(
                                    titulo = "Distancia",
                                    valor = String.format("%.1f km", distanciaTotal),
                                    icono = Icons.Default.Place,
                                    colorIcono = Distancia,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaGrupal(
                                    titulo = "Pasos",
                                    valor = String.format("%,d", pasosTotales),
                                    icono = Icons.Default.DirectionsRun,
                                    colorIcono = Pasos,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TarjetaMetricaGrupal(
                                    titulo = "Calorías",
                                    valor = String.format("%,d kcal", caloriasTotales),
                                    icono = Icons.Default.LocalFireDepartment,
                                    colorIcono = Calorias,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaGrupal(
                                    titulo = "Tiempo",
                                    valor = String.format("%d min", tiempoTotalMinutos),
                                    icono = Icons.Default.AccessTime,
                                    colorIcono = Tiempo,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Tabla de clasificación (Ranking)
                    item {
                        Text(
                            text = "Tabla de Clasificación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    // Ordenar por distancia de mayor a menor
                    val miembrosOrdenados = estadoUi.listaRanking

                    itemsIndexed(miembrosOrdenados) { posicion, miembro ->
                        FilaRankingMiembro(
                            posicion = posicion + 1,
                            miembro = miembro
                        )
                    }
                }
            } else {
                // MIEMBROS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(miembros) { miembro ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circular con la inicial
                                val inicial = miembro.nombre.firstOrNull()?.toString() ?: "?"
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SecondaryContainer, shape = CircleShape)
                                        .border(1.dp, Secondary, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = inicial,
                                        color = OnBackground,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = miembro.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "@${miembro.nombreUsuario}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }

                                // Mostrar el botón de eliminar miembro si es dueño y no es él mismo
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para Salir del Grupo
    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            containerColor = SurfaceVariant,
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Salir del grupo",
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir del grupo '$nombreGrupo'? Dejarás de compartir tus estadísticas en esta comunidad.",
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        grupoViewModel.salirDeGrupo(idUsuarioActual, idGrupo) {
                            mostrarDialogoSalir = false
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("SALIR DEL GRUPO", fontWeight = FontWeight.Bold, color = OnSurface)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogoSalir = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Diálogo para Confirmar Eliminación de Miembro
    /*
    miembroAEliminar?.let { miembro ->
        AlertDialog(
            onDismissRequest = { miembroAEliminar = null },
            containerColor = SurfaceVariant,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Eliminar miembro",
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar a ${miembro.nombre} del grupo? Sus estadísticas ya no contarán para el total del grupo.",
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        grupoViewModel.eliminarMiembroDeGrupo(idGrupo, miembro.idUsuario)
                        miembroAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("ELIMINAR", fontWeight = FontWeight.Bold, color = OnSurface)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { miembroAEliminar = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text("CANCELAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    */
}

@Composable
fun TarjetaMetricaGrupal(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FilaRankingMiembro(
    posicion: Int,
    miembro: MiembroGrupoResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (posicion == 1) SurfaceContainer else SurfaceVariant
        ),
        border = if (posicion == 1) androidx.compose.foundation.BorderStroke(1.dp, Warning) else null
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de posición
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = when (posicion) {
                            1 -> Warning // Oro/Amarillo
                            2 -> Color(0xFFC0C0C0) // Plata
                            3 -> Color(0xFFCD7F32) // Bronce
                            else -> Background
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (posicion <= 3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OnBackground,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = posicion.toString(),
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Datos del miembro
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = miembro.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                        text = "${miembro.pasos} pasos",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            // Distancia del ranking
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.1f", miembro.distancia),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Distancia
                )
                Text(
                    text = "km",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

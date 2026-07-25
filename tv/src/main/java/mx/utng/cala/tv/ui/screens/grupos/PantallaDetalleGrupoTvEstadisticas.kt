package mx.utng.cala.tv.ui.screens.grupos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.tv.ui.components.ElementoLeyendaGrafica
import mx.utng.cala.tv.ui.components.PuntoDatosGrafica
import mx.utng.cala.tv.ui.components.TarjetaGraficaRendimiento
import mx.utng.cala.tv.ui.theme.*
import mx.utng.cala.tv.ui.viewmodel.EstadoUiGrupoTv
import kotlin.math.roundToInt

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PantallaDetalleGrupoTv(
    idGrupo: Int,
    idUsuarioActual: Int,
    idCreadorGrupo: Int?,
    nombreGrupo: String,
    codigoGrupo: String,
    descripcionGrupo: String?,
    estadoUi: EstadoUiGrupoTv,
    solicitadorEnfoque: FocusRequester,
    alCargarEstadisticasMiembro: (Int) -> Unit,
    alSalirGrupo: () -> Unit,
    alEliminarGrupo: () -> Unit,
    alVolver: () -> Unit
) {
    var pestanaSeleccionada by remember { mutableStateOf(0) }
    var miembroSeleccionadoIndice by remember { mutableStateOf(0) }
    var mostrarConfirmarSalir by remember { mutableStateOf(false) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }
    var botonVolverEnfocado by remember { mutableStateOf(false) }

    val miembros = estadoUi.listaMiembros
    val miembroSeleccionado = miembros.getOrNull(miembroSeleccionadoIndice)
    val distanciaTotal = miembros.sumOf { it.distancia }
    val pasosTotal = miembros.sumOf { it.pasos }
    val caloriasTotal = miembros.sumOf { it.calorias }
    val tiempoTotalSegundos = miembros.sumOf { it.tiempo }
    val tiempoTotalMinutos = tiempoTotalSegundos / 60
    val esDueno = idUsuarioActual == idCreadorGrupo

    val puntosGrupo = remember(miembros) { generarPuntosGraficaGrupoTv(miembros) }
    val estadisticasMiembro = miembroSeleccionado?.let {
        estadoUi.estadisticasMiembro[it.idUsuario]
    }
    val puntosMiembro = remember(estadisticasMiembro) {
        generarPuntosGraficaMiembroTv(estadisticasMiembro)
    }
    val estaCargandoEstadisticas = miembroSeleccionado?.let {
        estadoUi.miembroEstadisticasCargandoId == it.idUsuario
    } == true
    val distanciaMiembro = estadisticasMiembro?.distanciaTotal ?: miembroSeleccionado?.distancia ?: 0.0
    val pasosMiembro = estadisticasMiembro?.pasosTotales ?: miembroSeleccionado?.pasos ?: 0
    val caloriasMiembro = estadisticasMiembro?.caloriasTotales ?: miembroSeleccionado?.calorias ?: 0
    val tiempoMiembro = estadisticasMiembro?.tiempoTotal ?: miembroSeleccionado?.tiempo ?: 0
    val grupoSinDatos = distanciaTotal == 0.0 &&
        pasosTotal == 0 &&
        caloriasTotal == 0 &&
        tiempoTotalSegundos == 0
    val miembroSinDatos = estadisticasMiembro?.let {
        it.distanciaTotal == 0.0 && it.pasosTotales == 0 &&
            it.caloriasTotales == 0 && it.tiempoTotal == 0
    } ?: true

    val focoAnterior = remember { FocusRequester() }
    val focoSiguiente = remember { FocusRequester() }
    val scrollActividad = rememberLazyListState()
    val scrollEstadisticas = rememberLazyListState()

    fun navegarMiembroAnterior() {
        if (miembros.isNotEmpty()) {
            miembroSeleccionadoIndice =
                (miembroSeleccionadoIndice - 1 + miembros.size) % miembros.size
        }
    }

    fun navegarMiembroSiguiente() {
        if (miembros.isNotEmpty()) {
            miembroSeleccionadoIndice = (miembroSeleccionadoIndice + 1) % miembros.size
        }
    }

    LaunchedEffect(miembros.size) {
        miembroSeleccionadoIndice = if (miembros.isEmpty()) 0
        else miembroSeleccionadoIndice.coerceIn(0, miembros.lastIndex)
    }

    LaunchedEffect(pestanaSeleccionada, miembroSeleccionado?.idUsuario, idGrupo) {
        if (pestanaSeleccionada == 2 && miembroSeleccionado != null) {
            alCargarEstadisticasMiembro(miembroSeleccionado.idUsuario)
        }
    }

    LaunchedEffect(pestanaSeleccionada, miembros.size) {
        if (pestanaSeleccionada == 2 && miembros.isNotEmpty()) {
            delay(100)
            runCatching { focoAnterior.requestFocus() }
        }
    }

    LaunchedEffect(pestanaSeleccionada, miembroSeleccionado?.idUsuario) {
        when (pestanaSeleccionada) {
            0 -> scrollActividad.scrollToItem(0)
            2 -> scrollEstadisticas.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = alVolver,
                modifier = Modifier
                    .size(40.dp)
                    .onFocusChanged { botonVolverEnfocado = it.isFocused }
                    .focusRequester(solicitadorEnfoque),
                shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = SurfaceVariant,
                    focusedContainerColor = Primary,
                    pressedContainerColor = PrimaryContainer
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = if (botonVolverEnfocado) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Mi grupo",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (esDueno) mostrarConfirmarEliminar = true
                    else mostrarConfirmarSalir = true
                },
                colors = ButtonDefaults.colors(
                    containerColor = Error,
                    contentColor = Color.White
                ),
                scale = ButtonDefaults.scale(focusedScale = 1.0f),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (esDueno) Icons.Default.Delete else Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esDueno) "Eliminar grupo" else "Salir del grupo",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceVariant, RoundedCornerShape(16.dp))
                .border(1.dp, Outline, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreGrupo,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = miembros.size.toString() + " miembros | Codigo: " + codigoGrupo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    if (!descripcionGrupo.isNullOrBlank()) {
                        Text(
                            text = descripcionGrupo,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = pestanaSeleccionada,
            modifier = Modifier
                .fillMaxWidth()
                .background(Background),
            indicator = { posiciones ->
                if (pestanaSeleccionada < posiciones.size) {
                    TabRowDefaults.PillIndicator(
                        currentTabPosition = posiciones[pestanaSeleccionada],
                        activeColor = PrimaryContainer,
                        inactiveColor = Color.Transparent
                    )
                }
            }
        ) {
            Tab(
                selected = pestanaSeleccionada == 0,
                onFocus = { pestanaSeleccionada = 0 }
            ) {
                TextoPestanaTv("Actividad", pestanaSeleccionada == 0)
            }
            Tab(
                selected = pestanaSeleccionada == 1,
                onFocus = { pestanaSeleccionada = 1 }
            ) {
                TextoPestanaTv("Miembros", pestanaSeleccionada == 1)
            }
            Tab(
                selected = pestanaSeleccionada == 2,
                onFocus = { pestanaSeleccionada = 2 }
            ) {
                TextoPestanaTv("Estadisticas", pestanaSeleccionada == 2)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (pestanaSeleccionada) {
                0 -> {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = scrollActividad,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TarjetaMetricaDetalleTv(
                                    titulo = "Distancia total",
                                    valor = String.format("%.1f km", distanciaTotal),
                                    icono = Icons.Default.DirectionsRun,
                                    colorMetrica = Distancia,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaDetalleTv(
                                    titulo = "Calorias totales",
                                    valor = String.format("%,d kcal", caloriasTotal),
                                    icono = Icons.Default.LocalFireDepartment,
                                    colorMetrica = Calorias,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaDetalleTv(
                                    titulo = "Tiempo total",
                                    valor = (tiempoTotalMinutos / 60).toString() + "h " +
                                        (tiempoTotalMinutos % 60).toString() + "min",
                                    icono = Icons.Default.AccessTime,
                                    colorMetrica = Tiempo,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                TarjetaGraficaRendimiento(
                                    puntos = puntosGrupo,
                                    titulo = "RENDIMIENTO DEL GRUPO",
                                    sinDatos = grupoSinDatos,
                                    leyenda = leyendaGraficaGrupoTv
                                )
                            }
                        }
                        // Botón 'Unirse a otro grupo' eliminado
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Miembros del grupo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (miembros.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay miembros en este grupo.",
                                    color = OnSurfaceVariant
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(miembros.size) { indice ->
                                    FilaMiembroGrupoTv(miembros[indice])
                                }
                            }
                        }
                        // Botón 'Unirse a otro grupo' eliminado
                    }
                }

                else -> {
                    if (miembroSeleccionado == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay miembros en este grupo.", color = OnSurfaceVariant)
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            state = scrollEstadisticas,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                                ) {
                                SelectorMiembroTv(
                                    texto = "<",
                                    onClick = ::navegarMiembroAnterior,
                                    focusRequester = focoAnterior
                                )
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp)
                                        .widthIn(min = 220.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = miembroSeleccionado.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "@" + miembroSeleccionado.nombreUsuario + " - " +
                                            (miembroSeleccionadoIndice + 1) + "/" + miembros.size,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                SelectorMiembroTv(
                                    texto = ">",
                                    onClick = ::navegarMiembroSiguiente,
                                    focusRequester = focoSiguiente
                                )
                                }
                            }

                            item {
                                Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                TarjetaMetricaDetalleTv(
                                    titulo = "Distancia",
                                    valor = String.format("%.1f km", distanciaMiembro),
                                    icono = Icons.Default.DirectionsRun,
                                    colorMetrica = Distancia,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaDetalleTv(
                                    titulo = "Pasos",
                                    valor = String.format("%,d", pasosMiembro),
                                    icono = Icons.Default.DirectionsRun,
                                    colorMetrica = Pasos,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaDetalleTv(
                                    titulo = "Calorias",
                                    valor = String.format("%,d kcal", caloriasMiembro),
                                    icono = Icons.Default.LocalFireDepartment,
                                    colorMetrica = Calorias,
                                    modifier = Modifier.weight(1f)
                                )
                                TarjetaMetricaDetalleTv(
                                    titulo = "Tiempo",
                                    valor = formatearTiempoGrupoTv(tiempoMiembro),
                                    icono = Icons.Default.AccessTime,
                                    colorMetrica = Tiempo,
                                    modifier = Modifier.weight(1f)
                                )
                                }
                            }

                            item {
                                Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                ) {
                                if (estaCargandoEstadisticas) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Cargando estadisticas...",
                                            color = OnSurfaceVariant
                                        )
                                    }
                                } else {
                                    // Reinicia la métrica visible al cambiar de miembro para
                                    // que cada perfil comience mostrando sus estadísticas de
                                    // distancia de la semana actual.
                                    key(miembroSeleccionado.idUsuario) {
                                        TarjetaGraficaRendimiento(
                                            puntos = puntosMiembro,
                                            titulo = "RENDIMIENTO INDIVIDUAL",
                                            sinDatos = miembroSinDatos,
                                            leyenda = leyendaGraficaIndividualTv,
                                            mensajeSinDatos = estadoUi.mensajeErrorEstadisticas
                                                ?: "Sin actividad esta semana",
                                            detalleSinDatos = "Selecciona otro miembro o vuelve a intentarlo."
                                        )
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DialogoConfirmacionGrupoTv(
        visible = mostrarConfirmarSalir,
        titulo = "Salir del grupo",
        mensaje = "Estas seguro de que deseas salir del grupo " + nombreGrupo + "?",
        textoConfirmar = "SALIR DEL GRUPO",
        onDismiss = { mostrarConfirmarSalir = false },
        onConfirm = {
            mostrarConfirmarSalir = false
            alSalirGrupo()
        }
    )
    DialogoConfirmacionGrupoTv(
        visible = mostrarConfirmarEliminar,
        titulo = "Eliminar grupo",
        mensaje = "Quieres eliminar el grupo junto con todos sus miembros?",
        textoConfirmar = "ELIMINAR GRUPO",
        onDismiss = { mostrarConfirmarEliminar = false },
        onConfirm = {
            mostrarConfirmarEliminar = false
            alEliminarGrupo()
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TextoPestanaTv(texto: String, activa: Boolean) {
    Text(
        text = texto,
        fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal,
        style = MaterialTheme.typography.titleMedium,
        color = if (activa) Primary else OnSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SelectorMiembroTv(
    texto: String,
    onClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(64.dp)
            .height(52.dp)
            .focusRequester(focusRequester),
        colors = ButtonDefaults.colors(
            containerColor = PrimaryContainer,
            contentColor = Color.White,
            focusedContainerColor = Primary,
            focusedContentColor = Color.Black
        ),
        scale = ButtonDefaults.scale(focusedScale = 1.0f),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(10.dp))
    ) {
        Text(texto, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DialogoConfirmacionGrupoTv(
    visible: Boolean,
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = mensaje,
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text(textoConfirmar, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.OutlinedButton(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("CANCELAR", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private val leyendaGraficaGrupoTv = listOf(
    ElementoLeyendaGrafica(Distancia, "Distancia grupal (km)"),
    ElementoLeyendaGrafica(Pasos, "Pasos grupales (miles)"),
    ElementoLeyendaGrafica(Calorias, "Calorias grupales (kcal)"),
    ElementoLeyendaGrafica(Tiempo, "Tiempo grupal")
)

private val leyendaGraficaIndividualTv = listOf(
    ElementoLeyendaGrafica(Distancia, "Distancia individual (km)"),
    ElementoLeyendaGrafica(Pasos, "Pasos individuales (miles)"),
    ElementoLeyendaGrafica(Calorias, "Calorias individuales (kcal)"),
    ElementoLeyendaGrafica(Tiempo, "Tiempo individual")
)

private val diasSemanaGraficaTv = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
private val proporcionesSemanaGrupoTv = listOf(0.08, 0.12, 0.18, 0.20, 0.17, 0.16, 0.09)

private fun generarPuntosGraficaGrupoTv(
    miembros: List<MiembroGrupoResponse>
): List<PuntoDatosGrafica> {
    val distanciaTotal = miembros.sumOf { it.distancia }
    val pasosTotal = miembros.sumOf { it.pasos }
    val caloriasTotal = miembros.sumOf { it.calorias }
    val tiempoTotal = miembros.sumOf { it.tiempo }

    return diasSemanaGraficaTv.mapIndexed { indice, dia ->
        val proporcion = proporcionesSemanaGrupoTv[indice]
        PuntoDatosGrafica(
            etiqueta = dia,
            distancia = distanciaTotal * proporcion,
            pasos = (pasosTotal * proporcion).roundToInt(),
            calorias = (caloriasTotal * proporcion).roundToInt(),
            tiempo = (tiempoTotal * proporcion).roundToInt()
        )
    }
}

private fun generarPuntosGraficaMiembroTv(
    estadisticas: DashboardSemanalResponse?
): List<PuntoDatosGrafica> {
    return estadisticas?.rendimientoDiario?.map { punto ->
        PuntoDatosGrafica(
            etiqueta = punto.dia,
            distancia = punto.distancia,
            pasos = punto.pasos,
            calorias = punto.calorias,
            tiempo = punto.tiempo
        )
    } ?: emptyList()
}

private fun formatearTiempoGrupoTv(segundosTotales: Int): String {
    val horas = segundosTotales / 3600
    val minutos = (segundosTotales % 3600) / 60
    return "%02dh %02dmin".format(horas, minutos)
}



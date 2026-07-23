package mx.utng.cala.tv.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.*
import mx.utng.cala.core.data.dto.response.ComparacionRendimientoResponse
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.dto.response.RespuestaDashboardMensual
import mx.utng.cala.tv.ui.components.BarraLateralTv
import mx.utng.cala.tv.ui.navigation.TvRoutes
import mx.utng.cala.tv.ui.theme.*
import mx.utng.cala.tv.ui.viewmodel.DashboardViewModel
import mx.utng.cala.tv.ui.viewmodel.PeriodoDashboard
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class PuntoDatosGrafico(
    val etiqueta: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    idUsuario: Int,
    onCerrarSesion: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val estadoUi by viewModel.estadoUi.collectAsState()

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarDashboardSemanal(idUsuario)
        viewModel.cargarComparacionSemanal(idUsuario)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Barra Lateral de Navegación
        BarraLateralTv(
            navController = navController,
            rutaSeleccionada = TvRoutes.DASHBOARD,
            onCerrarSesion = onCerrarSesion
        )

        // Contenido Principal del Dashboard
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            // Cabecera: Título y Selector de Período
            FilaCabecera(
                periodo = estadoUi.periodoSeleccionado,
                alCambiarPeriodo = { nuevoPeriodo ->
                    viewModel.cambiarPeriodo(nuevoPeriodo, idUsuario)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (estadoUi.estaCargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando estadísticas...", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            } else if (estadoUi.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ocurrió un error al cargar los datos", style = MaterialTheme.typography.bodyLarge, color = Error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(estadoUi.error ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                // Seleccionar datos según el período activo
                val distanciaTotal = if (estadoUi.periodoSeleccionado == PeriodoDashboard.SEMANAL) {
                    estadoUi.semanal?.distanciaTotal ?: 0.0
                } else {
                    estadoUi.mensual?.distanciaTotal ?: 0.0
                }

                val pasosTotales = if (estadoUi.periodoSeleccionado == PeriodoDashboard.SEMANAL) {
                    estadoUi.semanal?.pasosTotales ?: 0
                } else {
                    estadoUi.mensual?.pasosTotales ?: 0
                }

                val caloriasTotales = if (estadoUi.periodoSeleccionado == PeriodoDashboard.SEMANAL) {
                    estadoUi.semanal?.caloriasTotales ?: 0
                } else {
                    estadoUi.mensual?.caloriasTotales ?: 0
                }

                val tiempoTotal = if (estadoUi.periodoSeleccionado == PeriodoDashboard.SEMANAL) {
                    estadoUi.semanal?.tiempoTotal ?: 0
                } else {
                    estadoUi.mensual?.tiempoTotal ?: 0
                }

                val comparacion = if (estadoUi.periodoSeleccionado == PeriodoDashboard.SEMANAL) {
                    estadoUi.comparacionSemanal
                } else {
                    estadoUi.comparacionMensual
                }

                val puntosGrafico = mapperPuntosGrafico(estadoUi.semanal, estadoUi.mensual, estadoUi.periodoSeleccionado)
                val sinDatos = distanciaTotal == 0.0 && pasosTotales == 0 && caloriasTotales == 0 && tiempoTotal == 0

                // Fila de tarjetas acumuladoras
                FilaTarjetasTotales(
                    distancia = distanciaTotal,
                    pasos = pasosTotales,
                    calorias = caloriasTotales,
                    tiempo = tiempoTotal,
                    comparacion = comparacion,
                    sinDatos = sinDatos
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Área inferior: Gráfico + Comparativa y Tendencia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta del gráfico de rendimiento
                    Box(
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight()
                    ) {
                        TarjetaGraficoRendimiento(
                            puntos = puntosGrafico,
                            periodo = estadoUi.periodoSeleccionado,
                            sinDatos = sinDatos
                        )
                    }

                    // Columna de comparativa y tendencia general
                    Column(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarjeta de progreso de comparación
                        Box(modifier = Modifier.weight(0.6f)) {
                            TarjetaProgresoComparativa(
                                comparacion = comparacion,
                                esMensual = estadoUi.periodoSeleccionado == PeriodoDashboard.MENSUAL,
                                sinDatos = sinDatos
                            )
                        }

                        // Tarjeta de Tendencia General
                        Box(modifier = Modifier.weight(0.4f)) {
                            TarjetaTendenciaGeneral(
                                comparacion = comparacion,
                                sinDatos = sinDatos
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilaCabecera(
    periodo: PeriodoDashboard,
    alCambiarPeriodo: (PeriodoDashboard) -> Unit
) {
    val tituloPeriodo = if (periodo == PeriodoDashboard.SEMANAL) "DASHBOARD SEMANAL" else "DASHBOARD MENSUAL"
    val rangoFechas = if (periodo == PeriodoDashboard.SEMANAL) obtenerRangoSemanaActual() else obtenerRangoMesActual()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = tituloPeriodo,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rangoFechas,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        // Selector de Pestañas (Tabs)
        Row(
            modifier = Modifier
                .background(SurfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BotonTabPeriodo(
                texto = "Semanal",
                activo = periodo == PeriodoDashboard.SEMANAL,
                alHacerClick = { alCambiarPeriodo(PeriodoDashboard.SEMANAL) }
            )
            BotonTabPeriodo(
                texto = "Mensual",
                activo = periodo == PeriodoDashboard.MENSUAL,
                alHacerClick = { alCambiarPeriodo(PeriodoDashboard.MENSUAL) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BotonTabPeriodo(
    texto: String,
    activo: Boolean,
    alHacerClick: () -> Unit
) {
    Surface(
        onClick = alHacerClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (activo) Surface else Color.Transparent,
            focusedContainerColor = Surface,
            pressedContainerColor = Surface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        modifier = Modifier.width(100.dp).height(36.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (activo) Color.White else OnSurfaceVariant
            )
        }
    }
}

@Composable
fun FilaTarjetasTotales(
    distancia: Double,
    pasos: Int,
    calorias: Int,
    tiempo: Int,
    comparacion: ComparacionRendimientoResponse?,
    sinDatos: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TarjetaMetrica(
                titulo = "Distancia total",
                valor = String.format("%.2f km", distancia),
                porcentajeComparacion = comparacion?.distanciaMejora ?: 0.0,
                icono = Icons.Default.Place,
                colorIcono = Distancia,
                sinDatos = sinDatos
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            TarjetaMetrica(
                titulo = "Pasos totales",
                valor = String.format("%,d", pasos),
                porcentajeComparacion = comparacion?.pasosMejora ?: 0.0,
                icono = Icons.Default.DirectionsRun,
                colorIcono = Pasos,
                sinDatos = sinDatos
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            TarjetaMetrica(
                titulo = "Calorías totales",
                valor = String.format("%,d kcal", calorias),
                porcentajeComparacion = comparacion?.caloriasMejora ?: 0.0,
                icono = Icons.Default.LocalFireDepartment,
                colorIcono = Calorias,
                sinDatos = sinDatos
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            TarjetaMetrica(
                titulo = "Tiempo total",
                valor = formatearTiempo(tiempo),
                porcentajeComparacion = comparacion?.tiempoMejora ?: 0.0,
                icono = Icons.Default.AccessTime,
                colorIcono = Tiempo,
                sinDatos = sinDatos
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    porcentajeComparacion: Double,
    icono: ImageVector,
    colorIcono: Color,
    sinDatos: Boolean = false
) {
    var tieneFoco by remember { mutableStateOf(false) }

    Surface(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .onFocusChanged { tieneFoco = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = SurfaceVariant,
            pressedContainerColor = SurfaceVariant
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de fondo redondeado
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorIcono.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Comparación con periodo anterior o estado sin datos/estable
                if (sinDatos) {
                    Text(
                        text = "Sin datos anteriores",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurfaceVariant
                    )
                } else {
                    val esMejora = porcentajeComparacion > 0
                    val esEstable = porcentajeComparacion == 0.0
                    val signo = if (esEstable) "→" else if (esMejora) "↑" else "↓"
                    val colorTexto = if (esEstable) Neutral else if (esMejora) Primary else Error
                    Text(
                        text = "$signo ${abs(porcentajeComparacion)}% vs anterior",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = colorTexto
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaGraficoRendimiento(
    puntos: List<PuntoDatosGrafico>,
    periodo: PeriodoDashboard,
    sinDatos: Boolean = false
) {
    val tituloGrafica = if (periodo == PeriodoDashboard.SEMANAL) "RENDIMIENTO POR DÍA" else "RENDIMIENTO POR SEMANA"

    Surface(
        onClick = {},
        modifier = Modifier.fillMaxSize(),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = Surface,
            pressedContainerColor = Surface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Título de la Gráfica y Legendas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tituloGrafica,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Legenda de colores (solo si hay datos)
                if (!sinDatos) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ElementoLegenda(color = Distancia, etiqueta = "Distancia (km)")
                        ElementoLegenda(color = Pasos, etiqueta = "Pasos (miles)")
                        ElementoLegenda(color = Calorias, etiqueta = "Calorías (kcal)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dibujo de la gráfica con Canvas o Estado Vacío
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                if (sinDatos) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = OnSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin actividad en este período",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aquí se graficará tu rendimiento diario en cuanto comiences a registrar entrenamientos.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                alignment = Alignment.CenterHorizontally
                            )
                        }
                    }
                } else {
                    GraficoCanvasPersonalizado(
                        puntos = puntos,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ElementoLegenda(color: Color, etiqueta: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun GraficoCanvasPersonalizado(
    puntos: List<PuntoDatosGrafico>,
    modifier: Modifier = Modifier
) {
    val colorRejilla = Outline
    val colorTexto = OnSurfaceVariant

    Canvas(modifier = modifier) {
        val anchoTotal = size.width
        val altoTotal = size.height
        val margenIzquierdo = 40f
        val margenInferior = 30f
        val margenSuperior = 20f
        val margenDerecho = 40f

        val areaGraficoAncho = anchoTotal - margenIzquierdo - margenDerecho
        val areaGraficoAlto = altoTotal - margenSuperior - margenInferior

        // 1. Dibujar líneas de cuadrícula horizontales
        val numLineasHorizontal = 4
        for (i in 0..numLineasHorizontal) {
            val y = margenSuperior + areaGraficoAlto - (areaGraficoAlto / numLineasHorizontal) * i
            drawLine(
                color = colorRejilla,
                start = Offset(margenIzquierdo, y),
                end = Offset(anchoTotal - margenDerecho, y),
                strokeWidth = 1f
            )
        }

        if (puntos.isEmpty()) return@Canvas

        // 2. Calcular los valores máximos para escalar
        val maxDistancia = (puntos.maxOfOrNull { it.distancia } ?: 1.0).coerceAtLeast(5.0)
        val maxPasos = (puntos.maxOfOrNull { it.pasos } ?: 1).coerceAtLeast(1000)
        val maxCalorias = (puntos.maxOfOrNull { it.calorias } ?: 1).coerceAtLeast(500)

        // 3. Dibujar las barras y calcular las posiciones para la línea de pasos
        val cantElementos = puntos.size
        val anchoGrupo = areaGraficoAncho / cantElementos
        val anchoBarra = (anchoGrupo * 0.18f).coerceAtLeast(4f)
        val espacioEntreBarras = 4f

        val puntosPasosLine = mutableListOf<Offset>()

        puntos.forEachIndexed { indice, punto ->
            val grupoCentroX = margenIzquierdo + anchoGrupo * indice + anchoGrupo / 2

            // Posicionamiento de las 3 barras consecutivas en el grupo
            // Barra 1: Distancia (Verde)
            val alturaDistancia = (punto.distancia / maxDistancia) * areaGraficoAlto
            val barDistanciaLeft = grupoCentroX - (anchoBarra * 1.5f) - espacioEntreBarras
            val barDistanciaTop = margenSuperior + areaGraficoAlto - alturaDistancia
            drawRoundRect(
                color = Distancia,
                topLeft = Offset(barDistanciaLeft, barDistanciaTop.toFloat()),
                size = Size(anchoBarra, alturaDistancia.toFloat()),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Barra 2: Pasos (Azul)
            val alturaPasos = (punto.pasos.toDouble() / maxPasos) * areaGraficoAlto
            val barPasosLeft = grupoCentroX - (anchoBarra / 2f)
            val barPasosTop = margenSuperior + areaGraficoAlto - alturaPasos
            drawRoundRect(
                color = Pasos,
                topLeft = Offset(barPasosLeft, barPasosTop.toFloat()),
                size = Size(anchoBarra, alturaPasos.toFloat()),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Guardar posición para la línea que unirá los pasos
            puntosPasosLine.add(Offset(grupoCentroX, barPasosTop.toFloat()))

            // Barra 3: Calorías (Naranja)
            val alturaCalorias = (punto.calorias.toDouble() / maxCalorias) * areaGraficoAlto
            val barCaloriasLeft = grupoCentroX + (anchoBarra / 2f) + espacioEntreBarras
            val barCaloriasTop = margenSuperior + areaGraficoAlto - alturaCalorias
            drawRoundRect(
                color = Calorias,
                topLeft = Offset(barCaloriasLeft, barCaloriasTop.toFloat()),
                size = Size(anchoBarra, alturaCalorias.toFloat()),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // 4. Dibujar línea azul conectando la parte superior de las barras de pasos
        if (puntosPasosLine.size > 1) {
            val path = Path().apply {
                moveTo(puntosPasosLine[0].x, puntosPasosLine[0].y)
                for (i in 1 until puntosPasosLine.size) {
                    lineTo(puntosPasosLine[i].x, puntosPasosLine[i].y)
                }
            }
            drawPath(
                path = path,
                color = Pasos,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Dibujar pequeños círculos sobre los puntos de pasos para mayor estética
        puntosPasosLine.forEach { punto ->
            drawCircle(
                color = Pasos,
                radius = 5.dp.toPx(),
                center = punto
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = punto
            )
        }
    }

    // Dibujar etiquetas X debajo de la gráfica
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(start = 40.dp, end = 40.dp)
            .offset(y = 200.dp), // Ajuste empírico de offset
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        puntos.forEach { punto ->
            Text(
                text = punto.etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = colorTexto
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaProgresoComparativa(
    comparacion: ComparacionRendimientoResponse?,
    esMensual: Boolean,
    sinDatos: Boolean = false
) {
    val tituloComparacion = if (esMensual) "COMPARACIÓN CON MES ANTERIOR" else "COMPARACIÓN CON SEMANA ANTERIOR"

    Surface(
        onClick = {},
        modifier = Modifier.fillMaxSize(),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = Surface,
            pressedContainerColor = Surface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = tituloComparacion,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (sinDatos) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin datos históricos de comparación",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            alignment = Alignment.CenterHorizontally
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Realiza entrenamientos en diferentes periodos para ver la comparación de tu progreso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            alignment = Alignment.CenterHorizontally
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    FilaComparacionMetrica(
                        etiqueta = "Distancia",
                        porcentaje = comparacion?.distanciaMejora ?: 0.0
                    )
                    FilaComparacionMetrica(
                        etiqueta = "Pasos",
                        porcentaje = comparacion?.pasosMejora ?: 0.0
                    )
                    FilaComparacionMetrica(
                        etiqueta = "Calorías",
                        porcentaje = comparacion?.caloriasMejora ?: 0.0
                    )
                    FilaComparacionMetrica(
                        etiqueta = "Tiempo",
                        porcentaje = comparacion?.tiempoMejora ?: 0.0
                    )
                }
            }
        }
    }
}

@Composable
fun FilaComparacionMetrica(
    etiqueta: String,
    porcentaje: Double
) {
    val esMejora = porcentaje > 0
    val esEstable = porcentaje == 0.0
    val colorBarra = if (esEstable) Neutral else if (esMejora) Primary else Error
    val signo = if (esEstable) "→" else if (esMejora) "↑" else "↓"
    val colorTexto = if (esEstable) Neutral else if (esMejora) Primary else Error

    // Valor del progreso clamped [0.1, 1.0] para dibujarse estéticamente
    val fraccionProgreso = if (esEstable) 0.1f else (abs(porcentaje) / 100f).coerceIn(0.1, 1.0).toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface,
            modifier = Modifier.width(80.dp)
        )

        // Barra de progreso personalizada
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Outline, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccionProgreso)
                    .background(colorBarra, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Porcentaje a la derecha
        Text(
            text = "$signo ${abs(porcentaje)}%",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colorTexto,
            modifier = Modifier.width(60.dp),
            alignment = Alignment.End
        )
    }
}

enum class EstadoTendencia {
    MEJORA,
    EMPEORA,
    ESTABLE,
    SIN_DATOS
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaTendenciaGeneral(
    comparacion: ComparacionRendimientoResponse?,
    sinDatos: Boolean = false
) {
    // Evaluar el estado de la tendencia basándonos en la comparación y si hay datos
    val estado = when {
        sinDatos -> EstadoTendencia.SIN_DATOS
        comparacion == null -> EstadoTendencia.SIN_DATOS
        else -> {
            val promedio = (comparacion.distanciaMejora + comparacion.pasosMejora + comparacion.caloriasMejora) / 3.0
            when {
                promedio > 0.0 -> EstadoTendencia.MEJORA
                promedio < 0.0 -> EstadoTendencia.EMPEORA
                else -> EstadoTendencia.ESTABLE
            }
        }
    }

    val tituloTendencia = when (estado) {
        EstadoTendencia.MEJORA -> "Mejora"
        EstadoTendencia.EMPEORA -> "Disminución"
        EstadoTendencia.ESTABLE -> "Estable"
        EstadoTendencia.SIN_DATOS -> "¡Comienza hoy!"
    }

    val colorTendencia = when (estado) {
        EstadoTendencia.MEJORA -> Primary
        EstadoTendencia.EMPEORA -> Error
        EstadoTendencia.ESTABLE -> Neutral
        EstadoTendencia.SIN_DATOS -> Secondary
    }

    val iconoTendencia = when (estado) {
        EstadoTendencia.MEJORA -> Icons.Default.TrendingUp
        EstadoTendencia.EMPEORA -> Icons.Default.TrendingDown
        EstadoTendencia.ESTABLE -> Icons.Default.TrendingFlat
        EstadoTendencia.SIN_DATOS -> Icons.Default.DirectionsRun
    }

    val descripcionMotivacional = when (estado) {
        EstadoTendencia.MEJORA -> "Tu rendimiento ha mejorado respecto al periodo anterior. ¡Sigue así!"
        EstadoTendencia.EMPEORA -> "Tu rendimiento ha disminuido un poco. ¡Tú puedes lograr tus metas!"
        EstadoTendencia.ESTABLE -> "Has mantenido un rendimiento constante respecto al periodo anterior. ¡Sigue adelante!"
        EstadoTendencia.SIN_DATOS -> "Registra tu primer entrenamiento para empezar a calcular tu tendencia."
    }

    Surface(
        onClick = {},
        modifier = Modifier.fillMaxSize(),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Surface,
            focusedContainerColor = Surface,
            pressedContainerColor = Surface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de tendencia gigante con el color de estado correspondiente
            Icon(
                imageVector = iconoTendencia,
                contentDescription = null,
                tint = colorTendencia,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TENDENCIA GENERAL",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tituloTendencia,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorTendencia
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcionMotivacional,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface,
                    maxLines = 2
                )
            }
        }
    }
}

// Helpers para formateo de rangos y tiempos

fun obtenerRangoSemanaActual(): String {
    val calendario = Calendar.getInstance()
    val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
    val diferencia = if (diaSemana == Calendar.SUNDAY) -6 else Calendar.MONDAY - diaSemana
    calendario.add(Calendar.DAY_OF_MONTH, diferencia)
    val lunes = calendario.time

    calendario.add(Calendar.DAY_OF_MONTH, 6)
    val domingo = calendario.time

    val formatoDia = SimpleDateFormat("d", Locale("es", "MX"))
    val formatoMes = SimpleDateFormat("MMMM", Locale("es", "MX"))
    val formatoAnio = SimpleDateFormat("yyyy", Locale("es", "MX"))

    val mesLunes = formatoMes.format(lunes)
    val mesDomingo = formatoMes.format(domingo)
    val anioLunes = formatoAnio.format(lunes)
    val anioDomingo = formatoAnio.format(domingo)

    return if (mesLunes == mesDomingo) {
        "${formatoDia.format(lunes)} – ${formatoDia.format(domingo)} de ${mesLunes} de ${anioLunes}"
    } else {
        if (anioLunes == anioDomingo) {
            "${formatoDia.format(lunes)} de ${mesLunes} – ${formatoDia.format(domingo)} de ${mesDomingo} de ${anioLunes}"
        } else {
            "${formatoDia.format(lunes)} de ${mesLunes} de ${anioLunes} – ${formatoDia.format(domingo)} de ${mesDomingo} de ${anioDomingo}"
        }
    }
}

fun obtenerRangoMesActual(): String {
    val formatoMesAnio = SimpleDateFormat("MMMM 'de' yyyy", Locale("es", "MX"))
    return formatoMesAnio.format(Date()).replaceFirstChar { it.uppercase() }
}

fun formatearTiempo(segundosTotales: Int): String {
    val horas = segundosTotales / 3600
    val minutos = (segundosTotales % 3600) / 60
    val segundos = segundosTotales % 60
    return String.format("%02d:%02d:%02d", horas, minutos, segundos)
}

fun mapperPuntosGrafico(
    semanal: DashboardSemanalResponse?,
    mensual: RespuestaDashboardMensual?,
    periodo: PeriodoDashboard
): List<PuntoDatosGrafico> {
    return if (periodo == PeriodoDashboard.SEMANAL) {
        semanal?.rendimientoDiario?.map {
            // dia es "Lun", "Mar", etc.
            PuntoDatosGrafico(
                etiqueta = it.dia,
                distancia = it.distancia,
                pasos = it.pasos,
                calorias = it.calorias
            )
        } ?: emptyList()
    } else {
        mensual?.rendimientoSemanal?.map {
            // semana es "Semana 1", "Semana 2", etc.
            PuntoDatosGrafico(
                etiqueta = it.semana.replace("Semana ", "Sem "),
                distancia = it.distancia,
                pasos = it.pasos,
                calorias = it.calorias
            )
        } ?: emptyList()
    }
}

// Extensión para Text Alignment a la derecha
@Composable
fun Text(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier,
    alignment: Alignment.Horizontal
) {
    Box(
        modifier = modifier,
        contentAlignment = when (alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Text(text = text, style = style, color = color)
    }
}

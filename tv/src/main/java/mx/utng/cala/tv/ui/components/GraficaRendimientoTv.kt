package mx.utng.cala.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.utng.cala.tv.ui.theme.Calorias
import mx.utng.cala.tv.ui.theme.Distancia
import mx.utng.cala.tv.ui.theme.OnSurfaceVariant
import mx.utng.cala.tv.ui.theme.Outline
import mx.utng.cala.tv.ui.theme.Pasos
import mx.utng.cala.tv.ui.theme.Primary
import mx.utng.cala.tv.ui.theme.Surface as SurfaceColor
import mx.utng.cala.tv.ui.theme.SurfaceVariant
import mx.utng.cala.tv.ui.theme.Tiempo
import java.util.Locale
import kotlin.math.roundToInt

data class PuntoDatosGrafica(
    val etiqueta: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int = 0
)

data class ElementoLeyendaGrafica(
    val color: Color,
    val etiqueta: String
)

private val LeyendaGraficaPredeterminada = listOf(
    ElementoLeyendaGrafica(Distancia, "Distancia (km)"),
    ElementoLeyendaGrafica(Pasos, "Pasos"),
    ElementoLeyendaGrafica(Calorias, "Calorías (kcal)"),
    ElementoLeyendaGrafica(Tiempo, "Tiempo")
)

private enum class TipoMetricaGrafica {
    DISTANCIA,
    PASOS,
    CALORIAS,
    TIEMPO
}

private data class ConfiguracionMetricaGrafica(
    val tipo: TipoMetricaGrafica,
    val color: Color,
    val etiqueta: String
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TarjetaGraficaRendimiento(
    puntos: List<PuntoDatosGrafica>,
    titulo: String,
    sinDatos: Boolean = false,
    leyenda: List<ElementoLeyendaGrafica> = LeyendaGraficaPredeterminada,
    mensajeSinDatos: String = "Sin actividad en este período",
    detalleSinDatos: String = "Aquí se graficará el rendimiento en cuanto comiencen a registrarse entrenamientos."
) {
    val metricas = remember(leyenda) {
        leyenda.take(4).mapIndexed { indice, elemento ->
            ConfiguracionMetricaGrafica(
                tipo = TipoMetricaGrafica.entries[indice],
                color = elemento.color,
                etiqueta = elemento.etiqueta
            )
        }
    }
    var indiceMetrica by remember { mutableIntStateOf(0) }
    val indiceMetricaSeguro = indiceMetrica.coerceIn(0, metricas.lastIndex.coerceAtLeast(0))
    val metricaActual = metricas.getOrNull(indiceMetricaSeguro)

    LaunchedEffect(metricas.size) {
        indiceMetrica = indiceMetrica.coerceIn(0, metricas.lastIndex.coerceAtLeast(0))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceColor, RoundedCornerShape(16.dp))
    ) {
        // Las tarjetas de grupos pueden quedar con menos alto que la del dashboard.
        // En ese caso se usa una composición compacta para que el gráfico y sus
        // controles sigan teniendo un área real de dibujo.
        val esCompacta = maxHeight < 180.dp
        val relleno = if (esCompacta) 6.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            if (esCompacta) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (!sinDatos && metricaActual != null) {
                        Text(
                            text = metricaActual.etiqueta,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = metricaActual.color,
                            maxLines = 1
                        )
                    }
                    if (!sinDatos && metricas.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${indiceMetricaSeguro + 1}/${metricas.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (!sinDatos && metricaActual != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = metricaActual.etiqueta,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = metricaActual.color
                            )
                        }
                    }

                    if (!sinDatos && metricas.isNotEmpty()) {
                        Text(
                            text = "${indiceMetricaSeguro + 1}/${metricas.size}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (esCompacta) 4.dp else 12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (sinDatos || metricaActual == null) {
                    EstadoVacioGrafica(
                        mensaje = mensajeSinDatos,
                        detalle = detalleSinDatos
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (esCompacta) 4.dp else 10.dp)
                    ) {
                        BotonNavegacionGrafica(
                            texto = "<",
                            compacto = esCompacta,
                            onClick = {
                                indiceMetrica = if (indiceMetricaSeguro == 0) {
                                    metricas.lastIndex
                                } else {
                                    indiceMetricaSeguro - 1
                                }
                            }
                        )

                        GraficaMetricaPersonalizada(
                            puntos = puntos,
                            metrica = metricaActual,
                            compacto = esCompacta,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        BotonNavegacionGrafica(
                            texto = ">",
                            compacto = esCompacta,
                            onClick = {
                                indiceMetrica = if (indiceMetricaSeguro == metricas.lastIndex) {
                                    0
                                } else {
                                    indiceMetricaSeguro + 1
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EstadoVacioGrafica(
    mensaje: String,
    detalle: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            text = mensaje,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BotonNavegacionGrafica(
    texto: String,
    compacto: Boolean,
    onClick: () -> Unit
) {
    var tieneFoco by remember { mutableStateOf(false) }
    val colorFondo = if (tieneFoco) Primary else SurfaceVariant
    val colorBorde = if (tieneFoco) Primary else Outline

    Button(
        onClick = onClick,
        modifier = Modifier
            .width(if (compacto) 36.dp else 54.dp)
            .height(if (compacto) 36.dp else 54.dp)
            .onFocusChanged { tieneFoco = it.isFocused }
            .border(2.dp, colorBorde, RoundedCornerShape(10.dp)),
        colors = ButtonDefaults.colors(
            containerColor = colorFondo,
            contentColor = Color.White,
            focusedContainerColor = Primary,
            focusedContentColor = Color.Black
        ),
        scale = ButtonDefaults.scale(focusedScale = 1.0f),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(10.dp))
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Unspecified
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GraficaMetricaPersonalizada(
    puntos: List<PuntoDatosGrafica>,
    metrica: ConfiguracionMetricaGrafica,
    compacto: Boolean,
    modifier: Modifier = Modifier
) {
    val maximo = puntos.maxOfOrNull { valorDeMetrica(it, metrica.tipo) }
        ?.takeIf { it > 0.0 }
        ?: 1.0

    if (compacto) {
        Canvas(modifier = modifier.fillMaxSize()) {
            dibujarBarrasGrafica(puntos, metrica, maximo)
        }
    } else {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EscalaVerticalGrafica(
                    maximo = maximo,
                    tipo = metrica.tipo,
                    modifier = Modifier
                        .width(76.dp)
                        .fillMaxHeight()
                )

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    dibujarBarrasGrafica(puntos, metrica, maximo)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(start = 76.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                puntos.forEach { punto ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatearValorPunto(punto, metrica.tipo),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = metrica.color,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = punto.etiqueta,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.dibujarBarrasGrafica(
    puntos: List<PuntoDatosGrafica>,
    metrica: ConfiguracionMetricaGrafica,
    maximo: Double
) {
    val margenLateral = 6f
    val margenVertical = 3f
    val areaGraficoAncho = (size.width - margenLateral * 2).coerceAtLeast(1f)
    val areaGraficoAlto = (size.height - margenVertical * 2).coerceAtLeast(1f)
    val anchoGrupo = areaGraficoAncho / puntos.size.coerceAtLeast(1)
    val anchoBarra = (anchoGrupo * 0.52f).coerceAtLeast(3f)

    for (i in 0..4) {
        val y = margenVertical + areaGraficoAlto - (areaGraficoAlto / 4f) * i
        drawLine(
            color = Outline,
            start = Offset(margenLateral, y),
            end = Offset(size.width - margenLateral, y),
            strokeWidth = 1f
        )
    }

    puntos.forEachIndexed { indice, punto ->
        val valor = valorDeMetrica(punto, metrica.tipo)
        val alturaBarra = (valor / maximo * areaGraficoAlto)
            .toFloat()
            .coerceIn(0f, areaGraficoAlto)
        val centroX = margenLateral + anchoGrupo * indice + anchoGrupo / 2f
        val top = margenVertical + areaGraficoAlto - alturaBarra

        drawRoundRect(
            color = metrica.color,
            topLeft = Offset(centroX - anchoBarra / 2f, top),
            size = Size(anchoBarra, alturaBarra),
            cornerRadius = CornerRadius(5f, 5f)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun EscalaVerticalGrafica(
    maximo: Double,
    tipo: TipoMetricaGrafica,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        for (indice in 4 downTo 0) {
            Text(
                text = formatearValorEscala(maximo * indice / 4.0, tipo),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun valorDeMetrica(
    punto: PuntoDatosGrafica,
    tipo: TipoMetricaGrafica
): Double = when (tipo) {
    TipoMetricaGrafica.DISTANCIA -> punto.distancia
    TipoMetricaGrafica.PASOS -> punto.pasos.toDouble()
    TipoMetricaGrafica.CALORIAS -> punto.calorias.toDouble()
    TipoMetricaGrafica.TIEMPO -> punto.tiempo.toDouble()
}

private fun formatearValorEscala(valor: Double, tipo: TipoMetricaGrafica): String = when (tipo) {
    TipoMetricaGrafica.DISTANCIA -> String.format(Locale("es", "MX"), "%.2f", valor)
    TipoMetricaGrafica.PASOS,
    TipoMetricaGrafica.CALORIAS -> String.format(Locale("es", "MX"), "%,.0f", valor)
    TipoMetricaGrafica.TIEMPO -> formatearTiempo(valor.roundToInt())
}

private fun formatearValorPunto(
    punto: PuntoDatosGrafica,
    tipo: TipoMetricaGrafica
): String = when (tipo) {
    TipoMetricaGrafica.DISTANCIA -> String.format(Locale("es", "MX"), "%.2f km", punto.distancia)
    TipoMetricaGrafica.PASOS -> String.format(Locale("es", "MX"), "%,d", punto.pasos)
    TipoMetricaGrafica.CALORIAS -> "${punto.calorias} kcal"
    TipoMetricaGrafica.TIEMPO -> formatearTiempo(punto.tiempo)
}

private fun formatearTiempo(segundosTotales: Int): String {
    val horas = segundosTotales / 3600
    val minutos = (segundosTotales % 3600) / 60
    val segundos = segundosTotales % 60
    return String.format(Locale("es", "MX"), "%02d:%02d:%02d", horas, minutos, segundos)
}

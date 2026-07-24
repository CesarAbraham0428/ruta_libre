package mx.utng.cala.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import mx.utng.cala.tv.ui.theme.Calorias
import mx.utng.cala.tv.ui.theme.Distancia
import mx.utng.cala.tv.ui.theme.OnSurfaceVariant
import mx.utng.cala.tv.ui.theme.Outline
import mx.utng.cala.tv.ui.theme.Pasos
import mx.utng.cala.tv.ui.theme.Surface as SurfaceColor

data class PuntoDatosGrafica(
    val etiqueta: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int
)

data class ElementoLeyendaGrafica(
    val color: Color,
    val etiqueta: String
)

private val LeyendaGraficaPredeterminada = listOf(
    ElementoLeyendaGrafica(Distancia, "Distancia (km)"),
    ElementoLeyendaGrafica(Pasos, "Pasos (miles)"),
    ElementoLeyendaGrafica(Calorias, "Calorías (kcal)")
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
    Surface(
        onClick = {},
        modifier = Modifier.fillMaxSize(),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceColor,
            focusedContainerColor = SurfaceColor,
            pressedContainerColor = SurfaceColor
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.tv.material3.Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                if (!sinDatos && leyenda.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        leyenda.forEach { elemento ->
                            ElementoLeyendaGraficaView(elemento)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                if (sinDatos) {
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
                        androidx.tv.material3.Text(
                            text = mensajeSinDatos,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.tv.material3.Text(
                            text = detalleSinDatos,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    GraficaCanvasPersonalizada(
                        puntos = puntos,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementoLeyendaGraficaView(elemento: ElementoLeyendaGrafica) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(elemento.color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        androidx.tv.material3.Text(
            text = elemento.etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun GraficaCanvasPersonalizada(
    puntos: List<PuntoDatosGrafica>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val anchoTotal = size.width
                val altoTotal = size.height
                val margenIzquierdo = 40f
                val margenInferior = 10f
                val margenSuperior = 20f
                val margenDerecho = 40f

                val areaGraficoAncho = (anchoTotal - margenIzquierdo - margenDerecho).coerceAtLeast(1f)
                val areaGraficoAlto = (altoTotal - margenSuperior - margenInferior).coerceAtLeast(1f)

                val numLineasHorizontal = 4
                for (i in 0..numLineasHorizontal) {
                    val y = margenSuperior + areaGraficoAlto - (areaGraficoAlto / numLineasHorizontal) * i
                    drawLine(
                        color = Outline,
                        start = Offset(margenIzquierdo, y),
                        end = Offset(anchoTotal - margenDerecho, y),
                        strokeWidth = 1f
                    )
                }

                if (puntos.isEmpty()) return@Canvas

                val maxDistancia = (puntos.maxOfOrNull { it.distancia } ?: 1.0).coerceAtLeast(5.0)
                val maxPasos = (puntos.maxOfOrNull { it.pasos } ?: 1).coerceAtLeast(1000)
                val maxCalorias = (puntos.maxOfOrNull { it.calorias } ?: 1).coerceAtLeast(500)
                val anchoGrupo = areaGraficoAncho / puntos.size
                val anchoBarra = (anchoGrupo * 0.18f).coerceAtLeast(4f)
                val espacioEntreBarras = 4f
                val puntosPasosLine = mutableListOf<Offset>()

                puntos.forEachIndexed { indice, punto ->
                    val grupoCentroX = margenIzquierdo + anchoGrupo * indice + anchoGrupo / 2

                    val alturaDistancia = ((punto.distancia / maxDistancia) * areaGraficoAlto)
                        .toFloat().coerceIn(0f, areaGraficoAlto)
                    drawRoundRect(
                        color = Distancia,
                        topLeft = Offset(
                            grupoCentroX - (anchoBarra * 1.5f) - espacioEntreBarras,
                            margenSuperior + areaGraficoAlto - alturaDistancia
                        ),
                        size = Size(anchoBarra, alturaDistancia),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    val alturaPasos = (punto.pasos.toDouble() / maxPasos * areaGraficoAlto)
                        .toFloat().coerceIn(0f, areaGraficoAlto)
                    val barPasosTop = margenSuperior + areaGraficoAlto - alturaPasos
                    drawRoundRect(
                        color = Pasos,
                        topLeft = Offset(grupoCentroX - anchoBarra / 2f, barPasosTop),
                        size = Size(anchoBarra, alturaPasos),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    puntosPasosLine.add(Offset(grupoCentroX, barPasosTop))

                    val alturaCalorias = (punto.calorias.toDouble() / maxCalorias * areaGraficoAlto)
                        .toFloat().coerceIn(0f, areaGraficoAlto)
                    drawRoundRect(
                        color = Calorias,
                        topLeft = Offset(
                            grupoCentroX + anchoBarra / 2f + espacioEntreBarras,
                            margenSuperior + areaGraficoAlto - alturaCalorias
                        ),
                        size = Size(anchoBarra, alturaCalorias),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                if (puntosPasosLine.size > 1) {
                    val path = Path().apply {
                        moveTo(puntosPasosLine[0].x, puntosPasosLine[0].y)
                        for (i in 1 until puntosPasosLine.size) {
                            lineTo(puntosPasosLine[i].x, puntosPasosLine[i].y)
                        }
                    }
                    drawPath(path = path, color = Pasos, style = Stroke(width = 3.dp.toPx()))
                }

                puntosPasosLine.forEach { punto ->
                    drawCircle(color = Pasos, radius = 5.dp.toPx(), center = punto)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = punto)
                }
            }
        }

        if (puntos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(start = 40.dp, end = 40.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                puntos.forEach { punto ->
                    androidx.tv.material3.Text(
                        text = punto.etiqueta,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

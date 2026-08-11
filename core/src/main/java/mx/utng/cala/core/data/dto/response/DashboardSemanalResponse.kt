package mx.utng.cala.core.data.dto.response

/** Totales y desglose diario del rendimiento semanal. */
data class DashboardSemanalResponse(
    val distanciaTotal: Double,
    val pasosTotales: Int,
    val caloriasTotales: Int,
    val tiempoTotal: Int,
    val rendimientoDiario: List<RendimientoDiarioResponse>
)

/** Metricas acumuladas de un dia del periodo consultado. */
data class RendimientoDiarioResponse(
    val dia: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

package mx.utng.cala.core.data.dto.response

/** Totales y desglose semanal del rendimiento mensual. */
data class RespuestaDashboardMensual(
    val distanciaTotal: Double,
    val pasosTotales: Int,
    val caloriasTotales: Int,
    val tiempoTotal: Int,
    val rendimientoSemanal: List<RespuestaRendimientoSemanal>
)

/** Metricas acumuladas de una semana dentro del mes. */
data class RespuestaRendimientoSemanal(
    val semana: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

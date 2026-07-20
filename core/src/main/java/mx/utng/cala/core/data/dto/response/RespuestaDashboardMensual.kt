package mx.utng.cala.core.data.dto.response

data class RespuestaDashboardMensual(
    val distanciaTotal: Double,
    val pasosTotales: Int,
    val caloriasTotales: Int,
    val tiempoTotal: Int,
    val rendimientoSemanal: List<RespuestaRendimientoSemanal>
)

data class RespuestaRendimientoSemanal(
    val semana: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

package mx.utng.cala.core.data.dto.response

data class DashboardSemanalResponse(
    val distanciaTotal: Double,
    val pasosTotales: Int,
    val caloriasTotales: Int,
    val tiempoTotal: Int,
    val rendimientoDiario: List<RendimientoDiarioResponse>
)

data class RendimientoDiarioResponse(
    val dia: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

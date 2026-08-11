package mx.utng.cala.core.data.dto.response

/** Porcentajes de cambio de las metricas frente al periodo anterior. */
data class ComparacionRendimientoResponse(
    val distanciaMejora: Double,
    val pasosMejora: Double,
    val caloriasMejora: Double,
    val tiempoMejora: Double
)

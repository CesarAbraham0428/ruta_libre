package mx.utng.cala.core.data.model

/** Modelo de dominio de una meta personal de rendimiento. */
data class Meta(
    val idMetas: Int = 0,
    val idUsuario: Int = 0,
    val tipoMeta: TipoMeta = TipoMeta.DISTANCIA,
    val valorObjetivo: Double = 0.0,
    val valorActual: Double = 0.0,
    val terminada: Boolean = false
)

/** Tipos de metricas que puede perseguir una meta. */
enum class TipoMeta {
    PASOS, CALORIAS, DISTANCIA, TIEMPO
}

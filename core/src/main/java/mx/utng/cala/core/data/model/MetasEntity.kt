package mx.utng.cala.core.data.model

data class Meta(
    val idMetas: Int = 0,
    val idUsuario: Int = 0,
    val tipoMeta: TipoMeta = TipoMeta.DISTANCIA,
    val valorObjetivo: Double = 0.0,
    val valorActual: Double = 0.0,
    val terminada: Boolean = false
)

enum class TipoMeta {
    PASOS, CALORIAS, DISTANCIA, TIEMPO
}

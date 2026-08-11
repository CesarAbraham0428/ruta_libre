package mx.utng.cala.core.data.dto.response

/** Estado de una meta con su avance actual y objetivo. */
data class MetaResponse(
    val idMetas: Int,
    val idUsuario: Int,
    val tipoMeta: String,
    val valorObjetivo: Double,
    val valorActual: Double,
    val terminada: Boolean
)

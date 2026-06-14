package mx.utng.cala.core.data.dto.response

data class MetaResponse(
    val idMetas: Int,
    val idUsuario: Int,
    val tipoMeta: String,
    val valorObjetivo: Double,
    val valorActual: Double,
    val terminada: Boolean
)

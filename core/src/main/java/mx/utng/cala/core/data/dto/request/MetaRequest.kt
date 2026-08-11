package mx.utng.cala.core.data.dto.request

import mx.utng.cala.core.data.model.TipoMeta

/** Datos de una meta nueva asociada a un usuario. */
data class CrearMetaRequest(
    val idUsuario: Int,
    val tipoMeta: TipoMeta,
    val valorObjetivo: Double
)

/** Nuevo valor objetivo para una meta existente. */
data class ActualizarMetaRequest(
    val valorObjetivo: Double
)

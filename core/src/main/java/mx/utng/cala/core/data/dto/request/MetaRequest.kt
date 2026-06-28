package mx.utng.cala.core.data.dto.request

import mx.utng.cala.core.data.model.TipoMeta

data class CrearMetaRequest(
    val idUsuario: Int,
    val tipoMeta: TipoMeta,
    val valorObjetivo: Double
)

data class ActualizarMetaRequest(
    val valorObjetivo: Double
)

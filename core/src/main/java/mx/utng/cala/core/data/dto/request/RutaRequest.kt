package mx.utng.cala.core.data.dto.request

import mx.utng.cala.core.data.model.Coordenada

data class ActualizarRutaRequest(
    val idRuta: Int,
    val coordenadas: List<Coordenada>
)

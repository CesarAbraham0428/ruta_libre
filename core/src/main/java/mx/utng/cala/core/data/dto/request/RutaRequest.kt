package mx.utng.cala.core.data.dto.request

import mx.utng.cala.core.data.model.Coordenada

/** Ruta y coordenadas que se guardaran en el backend. */
data class ActualizarRutaRequest(
    val idRuta: Int,
    val coordenadas: List<Coordenada>
)

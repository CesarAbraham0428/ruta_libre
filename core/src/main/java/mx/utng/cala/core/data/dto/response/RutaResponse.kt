package mx.utng.cala.core.data.dto.response

/** Ruta serializada que devuelve el servicio remoto. */
data class RutaResponse(
    val idRuta: Int,
    val coordenadas: List<CoordenadaResponse>
)

/** Coordenada de ruta con formato de respuesta de la API. */
data class CoordenadaResponse(
    val longitud: Double,
    val latitud: Double
)

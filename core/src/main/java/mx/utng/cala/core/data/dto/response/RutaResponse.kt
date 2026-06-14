package mx.utng.cala.core.data.dto.response

data class RutaResponse(
    val idRuta: Int,
    val coordenadas: List<CoordenadaResponse>
)

data class CoordenadaResponse(
    val longitud: Double,
    val latitud: Double
)

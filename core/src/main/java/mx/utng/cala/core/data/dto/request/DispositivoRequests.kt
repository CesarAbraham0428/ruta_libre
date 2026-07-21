package mx.utng.cala.core.data.dto.request

data class SolicitarVinculacionRequest(
    val tipo: String = "tv",
    val nombre: String = "Ruta Libre TV"
)

data class EstadoVinculacionRequest(
    val idDispositivo: String,
    val secreto: String
)

data class VincularDispositivoRequest(val codigo: String)

data class VincularWearRequest(val nombre: String = "Ruta Libre Wear OS")

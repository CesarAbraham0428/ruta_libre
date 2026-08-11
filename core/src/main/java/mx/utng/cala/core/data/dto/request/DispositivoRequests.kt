package mx.utng.cala.core.data.dto.request

/** Datos para solicitar un codigo de vinculacion de un dispositivo. */
data class SolicitarVinculacionRequest(
    val tipo: String = "tv",
    val nombre: String = "Ruta Libre TV"
)

/** Identificadores usados para consultar el estado de una vinculacion. */
data class EstadoVinculacionRequest(
    val idDispositivo: String,
    val secreto: String
)

/** Codigo que el usuario introduce para vincular un dispositivo. */
data class VincularDispositivoRequest(val codigo: String)

/** Nombre enviado al registrar el dispositivo Wear OS. */
data class VincularWearRequest(val nombre: String = "Ruta Libre Wear OS")

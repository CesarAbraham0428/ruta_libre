package mx.utng.cala.core.data.dto.response

/** Respuesta con el codigo temporal para vincular un dispositivo. */
data class SolicitudVinculacionResponse(
    val idDispositivo: String,
    val codigo: String,
    val expira: String,
    val secreto: String
)

/** Estado actual de una solicitud de vinculacion. */
data class EstadoVinculacionResponse(
    val estado: String,
    val idUsuario: Int? = null,
    val token: String? = null
)

/** Dispositivo registrado y listado para un usuario. */
data class DispositivoResponse(
    val idDispositivo: String,
    val tipo: String,
    val nombre: String?,
    val activo: Boolean,
    val fechaVinculacion: String?
)

/** Datos del dispositivo que acaba de vincularse. */
data class DispositivoVinculadoResponse(
    val idDispositivo: String,
    val tipo: String,
    val nombre: String?,
    val fechaVinculacion: String?
)

/** Respuesta de vinculacion de un dispositivo Wear OS. */
data class WearVinculadoResponse(
    val idDispositivo: String,
    val idUsuario: Int,
    val token: String
)

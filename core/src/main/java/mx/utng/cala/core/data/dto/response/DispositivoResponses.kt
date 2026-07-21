package mx.utng.cala.core.data.dto.response

data class SolicitudVinculacionResponse(
    val idDispositivo: String,
    val codigo: String,
    val expira: String,
    val secreto: String
)

data class EstadoVinculacionResponse(
    val estado: String,
    val idUsuario: Int? = null,
    val token: String? = null
)

data class DispositivoResponse(
    val idDispositivo: String,
    val tipo: String,
    val nombre: String?,
    val activo: Boolean,
    val fechaVinculacion: String?
)

data class DispositivoVinculadoResponse(
    val idDispositivo: String,
    val tipo: String,
    val nombre: String?,
    val fechaVinculacion: String?
)

data class WearVinculadoResponse(
    val idDispositivo: String,
    val idUsuario: Int,
    val token: String
)

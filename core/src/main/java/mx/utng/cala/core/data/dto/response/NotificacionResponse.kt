package mx.utng.cala.core.data.dto.response

data class NotificacionResponse(
    val idNotificacion: Int,
    val idUsuario: Int,
    val idMetas: Int?,
    val mensaje: String,
    val fechaCreacion: String,
    val leidaMovil: Boolean,
    val leidaSmartwatch: Boolean
)

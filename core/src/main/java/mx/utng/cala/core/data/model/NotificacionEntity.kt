package mx.utng.cala.core.data.model

/** Modelo de una notificacion dirigida a un usuario. */
data class Notificacion(
    val idNotificacion: Int = 0,
    val idUsuario: Int = 0,
    val idMetas: Int? = null,
    val mensaje: String = "",
    val fechaCreacion: String = "",
    val leidaMovil: Boolean = false,
    val leidaSmartwatch: Boolean = false
)

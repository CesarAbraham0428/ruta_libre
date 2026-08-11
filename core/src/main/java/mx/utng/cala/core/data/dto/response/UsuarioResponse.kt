package mx.utng.cala.core.data.dto.response

/** Perfil de usuario devuelto por el backend. */
data class UsuarioResponse(
    val idUsuario: Int,
    val nombre: String,
    val nombreUsuario: String,
    val pesoKg: Double?,
    val fechaRegistro: String
)

package mx.utng.cala.core.data.dto.response

/** Datos de usuario y token obtenidos al iniciar sesion. */
data class LoginResponse(
    val idUsuario: Int,
    val nombre: String,
    val nombreUsuario: String,
    val pesoKg: Double?,
    val token: String
)

package mx.utng.cala.core.data.dto.request

/** Datos requeridos para registrar una cuenta nueva. */
data class RegisterRequest(
    val nombre: String,
    val nombreUsuario: String,
    val password: String
)

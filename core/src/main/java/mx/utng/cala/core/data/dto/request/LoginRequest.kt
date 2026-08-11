package mx.utng.cala.core.data.dto.request

/** Credenciales enviadas al servicio de autenticacion. */
data class LoginRequest(
    val nombreUsuario: String,
    val password: String
)

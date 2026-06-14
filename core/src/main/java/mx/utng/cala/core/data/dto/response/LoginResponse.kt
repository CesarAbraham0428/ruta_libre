package mx.utng.cala.core.data.dto.response

data class LoginResponse(
    val idUsuario: Int,
    val nombre: String,
    val nombreUsuario: String,
    val token: String
)

package mx.utng.cala.core.data.model

/** Modelo de dominio con la informacion basica del usuario. */
data class Usuario(
    val idUsuario: Int = 0,
    val nombre: String = "",
    val nombreUsuario: String = "",
    val password: String = "",
    val pesoKg: Double? = null,
    val fechaRegistro: String = ""
)

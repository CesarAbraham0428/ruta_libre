package mx.utng.cala.core.data.dto.request

import com.google.gson.annotations.SerializedName

/** Datos editables del perfil de un usuario. */
data class ActualizarUsuarioPeticion(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("password") val contrasena: String? = null,
    @SerializedName("pesoKg") val pesoKg: Double? = null
)

/** Peticion independiente para actualizar el peso del usuario. */
data class ActualizarPesoPeticion(
    @SerializedName("pesoKg") val pesoKg: Double
)

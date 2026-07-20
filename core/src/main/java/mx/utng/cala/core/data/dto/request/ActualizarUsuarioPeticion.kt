package mx.utng.cala.core.data.dto.request

import com.google.gson.annotations.SerializedName

data class ActualizarUsuarioPeticion(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("password") val contrasena: String? = null,
    @SerializedName("pesoKg") val pesoKg: Double? = null
)

data class ActualizarPesoPeticion(
    @SerializedName("pesoKg") val pesoKg: Double
)

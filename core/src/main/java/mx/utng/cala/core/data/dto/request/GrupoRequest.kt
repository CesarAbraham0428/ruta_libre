package mx.utng.cala.core.data.dto.request

data class CrearGrupoRequest(
    val nombre: String,
    val descripcion: String?,
    val idUsuario: Int
)

data class UnirseGrupoRequest(
    val idUsuario: Int,
    val codigo: String
)

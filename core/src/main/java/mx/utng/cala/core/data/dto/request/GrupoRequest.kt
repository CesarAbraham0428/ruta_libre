package mx.utng.cala.core.data.dto.request

data class CrearGrupoRequest(
    val nombre: String,
    val descripcion: String?
)

data class UnirseGrupoRequest(
    val idUsuario: Int,
    val codigo: String
)

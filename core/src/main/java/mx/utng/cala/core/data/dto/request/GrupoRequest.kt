package mx.utng.cala.core.data.dto.request

/** Datos necesarios para crear un grupo de entrenamiento. */
data class CrearGrupoRequest(
    val nombre: String,
    val descripcion: String?,
    val idUsuario: Int
)

/** Datos para agregar un usuario a un grupo mediante su codigo. */
data class UnirseGrupoRequest(
    val idUsuario: Int,
    val codigo: String
)

package mx.utng.cala.core.data.model

/** Modelo de dominio de un grupo de entrenamiento. */
data class Grupo(
    val idGrupo: Int = 0,
    val nombre: String = "",
    val codigo: String = "",
    val descripcion: String? = null
)

/** Relacion entre un usuario y un grupo. */
data class UsuarioGrupo(
    val idUsuarioGrupo: Int = 0,
    val idUsuario: Int = 0,
    val idGrupo: Int = 0,
    val fechaUnion: String = ""
)

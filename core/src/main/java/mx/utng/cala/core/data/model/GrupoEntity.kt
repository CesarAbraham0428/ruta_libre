package mx.utng.cala.core.data.model

data class Grupo(
    val idGrupo: Int = 0,
    val nombre: String = "",
    val codigo: String = "",
    val descripcion: String? = null
)

data class UsuarioGrupo(
    val idUsuarioGrupo: Int = 0,
    val idUsuario: Int = 0,
    val idGrupo: Int = 0,
    val fechaUnion: String = ""
)

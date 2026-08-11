package mx.utng.cala.core.data.dto.response

/** Informacion basica de un grupo disponible para el usuario. */
data class GrupoResponse(
    val idGrupo: Int,
    val nombre: String,
    val codigo: String,
    val descripcion: String?,
    val idCreador: Int? = null
)

/** Miembro de grupo junto con sus metricas de rendimiento. */
data class MiembroGrupoResponse(
    val idUsuario: Int,
    val nombre: String,
    val nombreUsuario: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

/** Lista ordenada de miembros que representa el ranking del grupo. */
data class RankingResponse(
    val miembros: List<MiembroGrupoResponse>
)

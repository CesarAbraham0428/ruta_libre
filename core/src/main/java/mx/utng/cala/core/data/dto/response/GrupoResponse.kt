package mx.utng.cala.core.data.dto.response

data class GrupoResponse(
    val idGrupo: Int,
    val nombre: String,
    val codigo: String,
    val descripcion: String?
)

data class MiembroGrupoResponse(
    val idUsuario: Int,
    val nombre: String,
    val nombreUsuario: String,
    val distancia: Double,
    val pasos: Int,
    val calorias: Int,
    val tiempo: Int
)

data class RankingResponse(
    val miembros: List<MiembroGrupoResponse>
)

package mx.utng.cala.core.data.repository

import com.google.gson.Gson
import mx.utng.cala.core.data.dto.request.CrearGrupoRequest
import mx.utng.cala.core.data.dto.request.UnirseGrupoRequest
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.dto.response.GrupoResponse
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.dto.response.RankingResponse
import mx.utng.cala.core.data.remote.RetrofitClient

/** Gestiona la consulta y administracion de grupos de entrenamiento. */
class GrupoRepository {

    private val api = RetrofitClient.apiService
    private val gson = Gson()

    /** Obtiene el mensaje de error del backend o usa un texto alternativo. */
    private fun errorMessage(response: retrofit2.Response<*>, fallback: String): String {
        return try {
            val body = response.errorBody()?.string()
            if (body.isNullOrBlank()) fallback
            else gson.fromJson(body, Map::class.java)["error"] as? String ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }

    /** Crea un grupo y registra al usuario como creador. */
    suspend fun crearGrupo(nombre: String, descripcion: String?, idUsuario: Int): Result<GrupoResponse> {
        return try {
            val response = api.crearGrupo(CrearGrupoRequest(nombre, descripcion, idUsuario))
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("La API no devolvió el grupo creado"))
            } else Result.failure(Exception(errorMessage(response, "Error al crear grupo")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Une al usuario a un grupo mediante un codigo de invitacion. */
    suspend fun unirseGrupo(idUsuario: Int, codigo: String): Result<Unit> {
        return try {
            val response = api.unirseGrupo(UnirseGrupoRequest(idUsuario, codigo))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Error al unirse al grupo")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Obtiene los grupos asociados al usuario. */
    suspend fun getGrupos(idUsuario: Int): Result<List<GrupoResponse>> {
        return try {
            val response = api.getGruposUsuario(idUsuario)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("La API no devolvió los grupos"))
            } else Result.failure(Exception(errorMessage(response, "Error al obtener grupos")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Consulta los miembros y sus metricas actuales. */
    suspend fun getMiembros(idGrupo: Int): Result<List<MiembroGrupoResponse>> {
        return try {
            val response = api.getMiembrosGrupo(idGrupo)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("La API no devolvió los miembros"))
            } else Result.failure(Exception(errorMessage(response, "Error al obtener miembros")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Obtiene las estadisticas semanales de un miembro. */
    suspend fun getEstadisticasMiembro(idUsuario: Int): Result<DashboardSemanalResponse> {
        return try {
            // Las estadísticas individuales deben ser exactamente las mismas
            // que aparecen en el dashboard del usuario.
            val response = api.getDashboardSemanal(idUsuario)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("La API no devolvio las estadisticas del miembro"))
            } else {
                Result.failure(Exception(errorMessage(response, "Error al obtener estadisticas del miembro")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Consulta el ranking de rendimiento del grupo. */
    suspend fun getRanking(idGrupo: Int): Result<RankingResponse> {
        return try {
            val response = api.getRankingGrupo(idGrupo)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("La API no devolvió el ranking"))
            } else Result.failure(Exception(errorMessage(response, "Error al obtener ranking")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Retira al usuario del grupo indicado. */
    suspend fun salirDeGrupo(idUsuario: Int, idGrupo: Int): Result<Unit> {
        return try {
            val response = api.salirDeGrupo(idGrupo, idUsuario)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Error al salir del grupo")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Elimina un grupo cuando el usuario tiene permisos de creador. */
    suspend fun eliminarGrupo(idGrupo: Int, idUsuario: Int): Result<Unit> {
        return try {
            val response = api.eliminarGrupo(idGrupo, idUsuario)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Error al eliminar el grupo")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

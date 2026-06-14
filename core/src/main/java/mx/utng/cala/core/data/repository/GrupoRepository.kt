package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.CrearGrupoRequest
import mx.utng.cala.core.data.dto.request.UnirseGrupoRequest
import mx.utng.cala.core.data.dto.response.GrupoResponse
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.dto.response.RankingResponse
import mx.utng.cala.core.data.remote.RetrofitClient

class GrupoRepository {

    private val api = RetrofitClient.apiService

    suspend fun crearGrupo(nombre: String, descripcion: String?): Result<GrupoResponse> {
        return try {
            val response = api.crearGrupo(CrearGrupoRequest(nombre, descripcion))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear grupo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unirseGrupo(idUsuario: Int, codigo: String): Result<Unit> {
        return try {
            val response = api.unirseGrupo(UnirseGrupoRequest(idUsuario, codigo))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al unirse al grupo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGrupos(idUsuario: Int): Result<List<GrupoResponse>> {
        return try {
            val response = api.getGruposUsuario(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener grupos"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMiembros(idGrupo: Int): Result<List<MiembroGrupoResponse>> {
        return try {
            val response = api.getMiembrosGrupo(idGrupo)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener miembros"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRanking(idGrupo: Int): Result<RankingResponse> {
        return try {
            val response = api.getRankingGrupo(idGrupo)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener ranking"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

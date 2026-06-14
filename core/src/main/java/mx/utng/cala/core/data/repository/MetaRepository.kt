package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.CrearMetaRequest
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.core.data.remote.RetrofitClient

class MetaRepository {

    private val api = RetrofitClient.apiService

    suspend fun crearMeta(idUsuario: Int, tipoMeta: TipoMeta, valorObjetivo: Double): Result<MetaResponse> {
        return try {
            val response = api.crearMeta(CrearMetaRequest(idUsuario, tipoMeta, valorObjetivo))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear meta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMetas(idUsuario: Int): Result<List<MetaResponse>> {
        return try {
            val response = api.getMetas(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener metas"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

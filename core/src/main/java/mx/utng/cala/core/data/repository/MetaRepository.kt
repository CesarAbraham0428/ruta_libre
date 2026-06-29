package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.ActualizarMetaRequest
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

    suspend fun actualizarMeta(idMetas: Int, valorObjetivo: Double): Result<MetaResponse> {
        return try {
            val response = api.actualizarMeta(idMetas, ActualizarMetaRequest(valorObjetivo))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al actualizar meta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarMeta(idMetas: Int): Result<Unit> {
        return try {
            val response = api.eliminarMeta(idMetas)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar meta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

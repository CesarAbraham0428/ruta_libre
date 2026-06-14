package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.ActualizarRutaRequest
import mx.utng.cala.core.data.dto.response.RutaResponse
import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.core.data.remote.RetrofitClient

class RutaRepository {

    private val api = RetrofitClient.apiService

    suspend fun actualizar(idRuta: Int, coordenadas: List<Coordenada>): Result<RutaResponse> {
        return try {
            val response = api.actualizarRuta(ActualizarRutaRequest(idRuta, coordenadas))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al actualizar ruta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRuta(idRuta: Int): Result<RutaResponse> {
        return try {
            val response = api.getRuta(idRuta)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener ruta"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.FinalizarEntrenamientoRequest
import mx.utng.cala.core.data.dto.request.IniciarEntrenamientoRequest
import mx.utng.cala.core.data.dto.response.*
import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.core.data.model.Punto
import mx.utng.cala.core.data.remote.RetrofitClient

class EntrenamientoRepository {

    private val api = RetrofitClient.apiService

    suspend fun iniciar(idUsuario: Int): Result<EntrenamientoResponse> {
        return try {
            val response = api.iniciarEntrenamiento(IniciarEntrenamientoRequest(idUsuario))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al iniciar entrenamiento"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizar(
        idEntrenamiento: Int,
        pasos: Int,
        calorias: Int,
        distancia: Double,
        tiempo: Int,
        coordenadas: List<Coordenada>,
        puntoInicio: Punto,
        puntoFin: Punto
    ): Result<EntrenamientoResponse> {
        return try {
            val request = FinalizarEntrenamientoRequest(
                idEntrenamiento, pasos, calorias, distancia, tiempo,
                coordenadas, puntoInicio, puntoFin
            )
            val response = api.finalizarEntrenamiento(request)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al finalizar entrenamiento"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivo(idUsuario: Int): Result<EntrenamientoActivoResponse> {
        return try {
            val response = api.getEntrenamientoActivo(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("No hay entrenamiento activo"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistorial(idUsuario: Int): Result<List<EntrenamientoResponse>> {
        return try {
            val response = api.getHistorialEntrenamientos(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener historial"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardSemanal(idUsuario: Int): Result<DashboardSemanalResponse> {
        return try {
            val response = api.getDashboardSemanal(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener dashboard"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComparacion(idUsuario: Int): Result<ComparacionRendimientoResponse> {
        return try {
            val response = api.getComparacionRendimiento(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener comparación"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

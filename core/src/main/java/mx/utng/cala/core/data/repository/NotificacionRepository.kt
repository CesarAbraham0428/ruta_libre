package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.response.NotificacionResponse
import mx.utng.cala.core.data.remote.RetrofitClient

class NotificacionRepository {

    private val api = RetrofitClient.apiService

    suspend fun getNotificaciones(idUsuario: Int): Result<List<NotificacionResponse>> {
        return try {
            val response = api.getNotificaciones(idUsuario)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al obtener notificaciones"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarLeidaMovil(idNotificacion: Int): Result<Unit> {
        return try {
            val response = api.marcarLeidaMovil(idNotificacion)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al marcar como leída"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarLeidaSmartwatch(idNotificacion: Int): Result<Unit> {
        return try {
            val response = api.marcarLeidaSmartwatch(idNotificacion)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al marcar como leída"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

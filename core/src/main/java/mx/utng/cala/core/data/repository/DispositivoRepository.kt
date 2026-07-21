package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.EstadoVinculacionRequest
import mx.utng.cala.core.data.dto.request.SolicitarVinculacionRequest
import mx.utng.cala.core.data.dto.request.VincularDispositivoRequest
import mx.utng.cala.core.data.dto.request.VincularWearRequest
import mx.utng.cala.core.data.dto.response.DispositivoVinculadoResponse
import mx.utng.cala.core.data.dto.response.DispositivoResponse
import mx.utng.cala.core.data.dto.response.EstadoVinculacionResponse
import mx.utng.cala.core.data.dto.response.SolicitudVinculacionResponse
import mx.utng.cala.core.data.dto.response.WearVinculadoResponse
import mx.utng.cala.core.data.remote.RetrofitClient

class DispositivoRepository {
    private val api = RetrofitClient.apiService

    suspend fun solicitarTv(nombre: String): Result<SolicitudVinculacionResponse> = runCatching {
        api.solicitarVinculacion(SolicitarVinculacionRequest(nombre = nombre)).bodyOrThrow()
    }

    suspend fun consultarEstado(
        idDispositivo: String,
        secreto: String
    ): Result<EstadoVinculacionResponse> = runCatching {
        api.consultarEstadoVinculacion(
            EstadoVinculacionRequest(idDispositivo, secreto)
        ).bodyOrThrow()
    }

    suspend fun vincularCodigo(
        token: String,
        codigo: String
    ): Result<DispositivoVinculadoResponse> = runCatching {
        api.vincularDispositivo("Bearer $token", VincularDispositivoRequest(codigo)).bodyOrThrow()
    }

    suspend fun vincularWear(token: String): Result<WearVinculadoResponse> = runCatching {
        api.vincularWear("Bearer $token", VincularWearRequest()).bodyOrThrow()
    }

    suspend fun listar(token: String): Result<List<DispositivoResponse>> = runCatching {
        api.listarDispositivos("Bearer $token").bodyOrThrow()
    }

    suspend fun desvincular(token: String, idDispositivo: String): Result<Unit> = runCatching {
        api.desvincularDispositivo("Bearer $token", idDispositivo).unitOrThrow()
    }

    suspend fun cerrarSesionDispositivo(token: String): Result<Unit> = runCatching {
        api.cerrarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    suspend fun validarSesionDispositivo(token: String): Result<Unit> = runCatching {
        api.validarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    suspend fun cerrarTodasLasSesiones(token: String): Result<Unit> = runCatching {
        api.cerrarTodasLasSesiones("Bearer $token").unitOrThrow()
    }

    private fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
        if (isSuccessful) return body() ?: error("El servidor respondió sin datos")
        val message = errorBody()?.string()?.let { body ->
            Regex("\\\"error\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
        }
        error(message ?: "Error del servidor (${code()})")
    }

    private fun retrofit2.Response<Unit>.unitOrThrow() {
        if (!isSuccessful) error("Error del servidor (${code()})")
    }
}

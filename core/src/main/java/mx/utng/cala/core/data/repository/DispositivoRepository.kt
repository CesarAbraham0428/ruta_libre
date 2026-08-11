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

/** Encapsula las operaciones de vinculacion y sesiones de dispositivos. */
class DispositivoRepository {
    private val api = RetrofitClient.apiService

    /** Solicita al backend un codigo temporal para la TV. */
    suspend fun solicitarTv(nombre: String): Result<SolicitudVinculacionResponse> = runCatching {
        api.solicitarVinculacion(SolicitarVinculacionRequest(nombre = nombre)).bodyOrThrow()
    }

    /** Consulta si una solicitud de vinculacion ya fue autorizada. */
    suspend fun consultarEstado(
        idDispositivo: String,
        secreto: String
    ): Result<EstadoVinculacionResponse> = runCatching {
        api.consultarEstadoVinculacion(
            EstadoVinculacionRequest(idDispositivo, secreto)
        ).bodyOrThrow()
    }

    /** Vincula un dispositivo con el codigo proporcionado por el usuario. */
    suspend fun vincularCodigo(
        token: String,
        codigo: String
    ): Result<DispositivoVinculadoResponse> = runCatching {
        api.vincularDispositivo("Bearer $token", VincularDispositivoRequest(codigo)).bodyOrThrow()
    }

    /** Registra la aplicacion Wear OS usando el token de sesion. */
    suspend fun vincularWear(token: String): Result<WearVinculadoResponse> = runCatching {
        api.vincularWear("Bearer $token", VincularWearRequest()).bodyOrThrow()
    }

    /** Obtiene todos los dispositivos vinculados a la cuenta. */
    suspend fun listar(token: String): Result<List<DispositivoResponse>> = runCatching {
        api.listarDispositivos("Bearer $token").bodyOrThrow()
    }

    /** Elimina la vinculacion de un dispositivo especifico. */
    suspend fun desvincular(token: String, idDispositivo: String): Result<Unit> = runCatching {
        api.desvincularDispositivo("Bearer $token", idDispositivo).unitOrThrow()
    }

    /** Cierra la sesion del dispositivo actual en el backend. */
    suspend fun cerrarSesionDispositivo(token: String): Result<Unit> = runCatching {
        api.cerrarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    /** Valida que el token del dispositivo continue activo. */
    suspend fun validarSesionDispositivo(token: String): Result<Unit> = runCatching {
        api.validarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    /** Revoca todas las sesiones de dispositivos del usuario. */
    suspend fun cerrarTodasLasSesiones(token: String): Result<Unit> = runCatching {
        api.cerrarTodasLasSesiones("Bearer $token").unitOrThrow()
    }

    /** Devuelve el cuerpo exitoso o lanza un error con el mensaje de la API. */
    private fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
        if (isSuccessful) return body() ?: error("El servidor respondió sin datos")
        val message = errorBody()?.string()?.let { body ->
            Regex("\\\"error\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
        }
        error(message ?: "Error del servidor (${code()})")
    }

    /** Valida una respuesta sin cuerpo y lanza error si no fue exitosa. */
    private fun retrofit2.Response<Unit>.unitOrThrow() {
        if (!isSuccessful) error("Error del servidor (${code()})")
    }
}

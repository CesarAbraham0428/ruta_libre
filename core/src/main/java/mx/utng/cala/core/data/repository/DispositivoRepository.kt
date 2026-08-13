package mx.utng.cala.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.utng.cala.core.data.dto.request.EstadoVinculacionRequest
import mx.utng.cala.core.data.dto.request.SolicitarVinculacionRequest
import mx.utng.cala.core.data.dto.request.VincularDispositivoRequest
import mx.utng.cala.core.data.dto.request.VincularWearRequest
import mx.utng.cala.core.data.dto.response.DispositivoVinculadoResponse
import mx.utng.cala.core.data.dto.response.DispositivoResponse
import mx.utng.cala.core.data.dto.response.EstadoVinculacionResponse
import mx.utng.cala.core.data.dto.response.SolicitudVinculacionResponse
import mx.utng.cala.core.data.dto.response.WearVinculadoResponse
import mx.utng.cala.core.data.remote.ApiException
import mx.utng.cala.core.data.remote.RetrofitClient

/** Encapsula las operaciones de vinculacion y sesiones de dispositivos. */
class DispositivoRepository {
    private val api = RetrofitClient.apiService

    /** Solicita al backend un codigo temporal para la TV. */
    suspend fun solicitarTv(nombre: String): Result<SolicitudVinculacionResponse> = apiResult {
        api.solicitarVinculacion(SolicitarVinculacionRequest(nombre = nombre)).bodyOrThrow()
    }

    /** Consulta si una solicitud de vinculacion ya fue autorizada. */
    suspend fun consultarEstado(
        idDispositivo: String,
        secreto: String
    ): Result<EstadoVinculacionResponse> = apiResult {
        api.consultarEstadoVinculacion(
            EstadoVinculacionRequest(idDispositivo, secreto)
        ).bodyOrThrow()
    }

    /** Vincula un dispositivo con el codigo proporcionado por el usuario. */
    suspend fun vincularCodigo(
        token: String,
        codigo: String
    ): Result<DispositivoVinculadoResponse> = apiResult {
        api.vincularDispositivo("Bearer $token", VincularDispositivoRequest(codigo)).bodyOrThrow()
    }

    /** Registra la aplicacion Wear OS usando el token de sesion. */
    suspend fun vincularWear(token: String): Result<WearVinculadoResponse> = apiResult {
        api.vincularWear("Bearer $token", VincularWearRequest()).bodyOrThrow()
    }

    /** Obtiene todos los dispositivos vinculados a la cuenta. */
    suspend fun listar(token: String): Result<List<DispositivoResponse>> = apiResult {
        api.listarDispositivos("Bearer $token").bodyOrThrow()
    }

    /** Elimina la vinculacion de un dispositivo especifico. */
    suspend fun desvincular(token: String, idDispositivo: String): Result<Unit> = apiResult {
        api.desvincularDispositivo("Bearer $token", idDispositivo).unitOrThrow()
    }

    /** Cierra la sesion del dispositivo actual en el backend. */
    suspend fun cerrarSesionDispositivo(token: String): Result<Unit> = apiResult {
        api.cerrarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    /** Valida que el token del dispositivo continue activo. */
    suspend fun validarSesionDispositivo(token: String): Result<Unit> = apiResult {
        api.validarSesionDispositivo("Bearer $token").unitOrThrow()
    }

    /** Revoca todas las sesiones de dispositivos del usuario. */
    suspend fun cerrarTodasLasSesiones(token: String): Result<Unit> = apiResult {
        api.cerrarTodasLasSesiones("Bearer $token").unitOrThrow()
    }

    /** Devuelve el cuerpo exitoso o lanza [ApiException] con el mensaje de la API. */
    private fun <T> retrofit2.Response<T>.bodyOrThrow(): T {
        if (isSuccessful) return body() ?: error("El servidor respondió sin datos")
        val message = errorBody()?.string()?.let { body ->
            Regex("\\\"error\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
        }
        throw ApiException(code(), message ?: "Error del servidor (${code()})")
    }

    /** Valida una respuesta sin cuerpo y lanza [ApiException] si no fue exitosa. */
    private fun retrofit2.Response<Unit>.unitOrThrow() {
        if (!isSuccessful) throw ApiException(code(), "Error del servidor (${code()})")
    }

    /**
     * Conserva la cancelación estructurada de corrutinas y encapsula los demás fallos
     * en [Result], para que la UI pueda mostrar estados recuperables de red.
     */
    private suspend fun <T> apiResult(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

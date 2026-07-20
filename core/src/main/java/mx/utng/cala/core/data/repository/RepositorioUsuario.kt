package mx.utng.cala.core.data.repository

import mx.utng.cala.core.data.dto.request.ActualizarUsuarioPeticion
import mx.utng.cala.core.data.dto.request.ActualizarPesoPeticion
import mx.utng.cala.core.data.dto.response.UsuarioResponse
import mx.utng.cala.core.data.remote.RetrofitClient

class RepositorioUsuario {

    private val servicioApi = RetrofitClient.apiService

    suspend fun obtenerUsuario(idUsuario: Int): Result<UsuarioResponse> {
        return try {
            val respuesta = servicioApi.getUsuario(idUsuario)
            if (respuesta.isSuccessful) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception("Error al obtener el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarUsuario(idUsuario: Int, nombre: String, contrasena: String?, pesoKg: Double?): Result<Unit> {
        return try {
            val peticion = ActualizarUsuarioPeticion(nombre, contrasena, pesoKg)
            val respuesta = servicioApi.actualizarUsuario(idUsuario, peticion)
            if (respuesta.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar el usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarPeso(idUsuario: Int, pesoKg: Double): Result<Unit> {
        return try {
            val respuesta = servicioApi.actualizarPeso(idUsuario, ActualizarPesoPeticion(pesoKg))
            if (respuesta.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("No se pudo guardar el peso"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

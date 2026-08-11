package mx.utng.cala.core.data.repository

import com.google.gson.Gson
import mx.utng.cala.core.data.dto.request.LoginRequest
import mx.utng.cala.core.data.dto.request.RegisterRequest
import mx.utng.cala.core.data.dto.response.LoginResponse
import mx.utng.cala.core.data.remote.RetrofitClient

/** Coordina las operaciones de registro e inicio de sesion. */
class AuthRepository {

    private val api = RetrofitClient.apiService
    private val gson = Gson()

    /** Extrae el mensaje de error enviado por la API. */
    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = gson.fromJson(errorBody, Map::class.java)
                errorResponse["error"] as? String ?: "Error desconocido"
            } else {
                "Error desconocido"
            }
        } catch (e: Exception) {
            "Error desconocido"
        }
    }

    /** Autentica al usuario y devuelve sus datos junto con el token. */
    suspend fun login(nombreUsuario: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(nombreUsuario, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Registra una cuenta nueva y transforma la respuesta en un Result. */
    suspend fun register(nombre: String, nombreUsuario: String, password: String): Result<Unit> {
        return try {
            val response = api.register(RegisterRequest(nombre, nombreUsuario, password))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

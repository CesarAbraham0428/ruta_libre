package mx.utng.cala.rutalibre.data.auth

import android.content.Context
import android.util.Base64
import org.json.JSONObject

/** Datos mínimos que se conservan para restaurar una sesión autenticada. */
data class AuthSession(
    val idUsuario: Int,
    val nombre: String,
    val pesoKg: Double?,
    val token: String
)

/** Persiste, recupera y elimina la sesión JWT del usuario en preferencias locales. */
class AuthSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /** Recupera la sesión guardada y descarta tokens ausentes, inválidos o vencidos. */
    fun restore(): AuthSession? {
        val token = preferences.getString(KEY_TOKEN, null)
            ?.takeIf(String::isNotBlank)
            ?: return null

        if (isExpiredOrInvalid(token)) {
            clear()
            return null
        }

        val idUsuario = preferences.getInt(KEY_USER_ID, -1).takeIf { it > 0 } ?: return null
        val nombre = preferences.getString(KEY_NAME, null)?.takeIf(String::isNotBlank) ?: return null
        val pesoKg = preferences.getString(KEY_WEIGHT, null)?.toDoubleOrNull()

        return AuthSession(idUsuario, nombre, pesoKg, token)
    }

    /** Guarda los datos de sesión necesarios para mantener el acceso del usuario. */
    fun save(session: AuthSession) {
        preferences.edit()
            .putInt(KEY_USER_ID, session.idUsuario)
            .putString(KEY_NAME, session.nombre)
            .apply {
                session.pesoKg?.let { putString(KEY_WEIGHT, it.toString()) } ?: remove(KEY_WEIGHT)
            }
            .putString(KEY_TOKEN, session.token)
            .apply()
    }

    /** Elimina todos los datos locales asociados con la sesión actual. */
    fun clear() {
        preferences.edit().clear().apply()
    }

    /** Valida la estructura del JWT y compara su fecha de expiración con la hora actual. */
    private fun isExpiredOrInvalid(token: String): Boolean = runCatching {
        val payload = requireNotNull(token.split('.').getOrNull(1))
        val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val expiresAt = JSONObject(String(decoded, Charsets.UTF_8)).getLong("exp")
        expiresAt <= System.currentTimeMillis() / 1_000L
    }.getOrDefault(true)

    /** Centraliza las claves y el nombre del almacenamiento de sesión. */
    private companion object {
        const val PREFERENCES_NAME = "ruta_libre_auth_session"
        const val KEY_USER_ID = "id_usuario"
        const val KEY_NAME = "nombre"
        const val KEY_WEIGHT = "peso_kg"
        const val KEY_TOKEN = "token"
    }
}

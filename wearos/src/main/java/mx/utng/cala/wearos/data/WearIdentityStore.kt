package mx.utng.cala.wearos.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

/** Persiste y recupera la identidad del usuario y del dispositivo Wear OS. */
class WearIdentityStore(context: Context) {
    private val preferences = context.getSharedPreferences("ruta_libre_wear_identity", Context.MODE_PRIVATE)

    /** Identificador del usuario vinculado, o null si no existe una sesión. */
    val idUsuario: Int?
        get() = preferences.getInt("id_usuario", -1).takeIf { it > 0 }

    /** Identificador del dispositivo guardado o extraído del token JWT. */
    val idDispositivo: String?
        get() = preferences.getString("id_dispositivo", null) ?: tokenDeviceId()

    /** Token JWT usado para autenticar las peticiones del reloj. */
    val token: String?
        get() = preferences.getString("token", null)

    /** Guarda las credenciales recibidas desde la aplicación móvil. */
    fun save(idUsuario: Int, idDispositivo: String, token: String) {
        preferences.edit()
            .putInt("id_usuario", idUsuario)
            .putString("id_dispositivo", idDispositivo)
            .putString("token", token)
            .apply()
    }

    /** Elimina la identidad local y deja el reloj sin sesión vinculada. */
    fun clear() = preferences.edit().clear().apply()

    /** Obtiene el ID del dispositivo leyendo el payload del token JWT. */
    private fun tokenDeviceId(): String? = runCatching {
        val payload = requireNotNull(token?.split('.')?.getOrNull(1))
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JSONObject(json).optString("idDispositivo").takeIf(String::isNotBlank)
    }.getOrNull()
}

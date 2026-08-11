package mx.utng.cala.tv.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

/** Persiste la identidad de la TV y su token de sesion local. */
class TvIdentityStore(context: Context) {
    private val preferences = context.getSharedPreferences("ruta_libre_tv_identity", Context.MODE_PRIVATE)

    val idUsuario: Int?
        get() = preferences.getInt("id_usuario", -1).takeIf { it > 0 }

    val idDispositivo: String?
        get() = preferences.getString("id_dispositivo", null) ?: tokenDeviceId()

    val token: String?
        get() = preferences.getString("token", null)

    /** Guarda el usuario, dispositivo y token obtenidos al vincular la TV. */
    fun save(idUsuario: Int, idDispositivo: String, token: String) {
        preferences.edit()
            .putInt("id_usuario", idUsuario)
            .putString("id_dispositivo", idDispositivo)
            .putString("token", token)
            .apply()
    }

    /** Elimina la identidad local para volver al flujo de vinculacion. */
    fun clear() = preferences.edit().clear().apply()

    /** Recupera el id del dispositivo desde el payload del token JWT. */
    private fun tokenDeviceId(): String? = runCatching {
        val payload = requireNotNull(token?.split('.')?.getOrNull(1))
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JSONObject(json).optString("idDispositivo").takeIf(String::isNotBlank)
    }.getOrNull()
}

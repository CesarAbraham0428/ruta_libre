package mx.utng.cala.wearos.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject

class WearIdentityStore(context: Context) {
    private val preferences = context.getSharedPreferences("ruta_libre_wear_identity", Context.MODE_PRIVATE)

    val idUsuario: Int?
        get() = preferences.getInt("id_usuario", -1).takeIf { it > 0 }

    val idDispositivo: String?
        get() = preferences.getString("id_dispositivo", null) ?: tokenDeviceId()

    val token: String?
        get() = preferences.getString("token", null)

    fun save(idUsuario: Int, idDispositivo: String, token: String) {
        preferences.edit()
            .putInt("id_usuario", idUsuario)
            .putString("id_dispositivo", idDispositivo)
            .putString("token", token)
            .apply()
    }

    fun clear() = preferences.edit().clear().apply()

    private fun tokenDeviceId(): String? = runCatching {
        val payload = requireNotNull(token?.split('.')?.getOrNull(1))
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JSONObject(json).optString("idDispositivo").takeIf(String::isNotBlank)
    }.getOrNull()
}

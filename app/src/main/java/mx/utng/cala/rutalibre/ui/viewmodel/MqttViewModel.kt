package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import mx.utng.cala.rutalibre.data.mqtt.MqttManager

/** Expone el estado y los eventos MQTT al ciclo de vida de Compose. */
class MqttViewModel : ViewModel() {
    private val manager = MqttManager()

    val connectionState = manager.connectionState
    val events = manager.events

    /** Inicia la conexión MQTT para el usuario autenticado. */
    fun connect(userId: Int) = manager.connect(userId)

    /** Cierra la conexión MQTT cuando no hay una sesión activa. */
    fun disconnect() = manager.disconnect()

    /** Libera la conexión MQTT al destruir el ViewModel. */
    override fun onCleared() {
        manager.disconnect()
        super.onCleared()
    }
}

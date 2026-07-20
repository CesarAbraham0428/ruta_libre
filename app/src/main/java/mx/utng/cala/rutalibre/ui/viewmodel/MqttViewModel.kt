package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import mx.utng.cala.rutalibre.data.mqtt.MqttManager

class MqttViewModel : ViewModel() {
    private val manager = MqttManager()

    val connectionState = manager.connectionState
    val events = manager.events

    fun connect(userId: Int) = manager.connect(userId)

    fun disconnect() = manager.disconnect()

    override fun onCleared() {
        manager.disconnect()
        super.onCleared()
    }
}

package mx.utng.cala.core.data.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.charset.StandardCharsets
import java.util.UUID

data class MqttConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val clientPrefix: String
)

enum class MqttSubscriberStatus { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

data class MqttSubscriberEvent(val topic: String, val payload: String)

class MqttSubscriber(
    private val config: MqttConfig,
    private val logTag: String
) {
    private val _status = MutableStateFlow(MqttSubscriberStatus.DISCONNECTED)
    val status: StateFlow<MqttSubscriberStatus> = _status

    private val _events = MutableSharedFlow<MqttSubscriberEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<MqttSubscriberEvent> = _events

    private var client: Mqtt3AsyncClient? = null
    private var currentUserId: Int? = null

    fun connect(userId: Int) {
        if (currentUserId == userId && client?.state == MqttClientState.CONNECTED) return
        disconnect()

        if (config.host.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            _status.value = MqttSubscriberStatus.ERROR
            Log.e(logTag, "Falta configurar MQTT en local.properties")
            return
        }

        currentUserId = userId
        _status.value = MqttSubscriberStatus.CONNECTING

        val newClient = Mqtt3Client.builder()
            .identifier("${config.clientPrefix}-${UUID.randomUUID()}")
            .serverHost(config.host)
            .serverPort(config.port)
            .sslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()

        client = newClient
        newClient.connectWith()
            .simpleAuth()
                .username(config.username)
                .password(config.password.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
            .cleanSession(true)
            .keepAlive(30)
            .send()
            .whenComplete { _, connectError ->
                if (connectError != null) {
                    _status.value = MqttSubscriberStatus.ERROR
                    Log.e(logTag, "No se pudo conectar a HiveMQ", connectError)
                } else {
                    subscribe(newClient, userId)
                }
            }
    }

    private fun subscribe(mqttClient: Mqtt3AsyncClient, userId: Int) {
        mqttClient.subscribeWith()
            .topicFilter("rutalibre/usuarios/$userId/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                val event = MqttSubscriberEvent(
                    topic = publish.topic.toString(),
                    payload = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                )
                Log.d(logTag, "Mensaje recibido en ${event.topic}: ${event.payload}")
                _events.tryEmit(event)
            }
            .send()
            .whenComplete { _, subscribeError ->
                if (subscribeError == null) {
                    _status.value = MqttSubscriberStatus.CONNECTED
                    Log.d(logTag, "Suscripción activa para el usuario $userId")
                } else {
                    _status.value = MqttSubscriberStatus.ERROR
                    Log.e(logTag, "No se pudo crear la suscripción", subscribeError)
                }
            }
    }

    fun disconnect() {
        val previousClient = client
        client = null
        currentUserId = null
        if (previousClient?.state == MqttClientState.CONNECTED) previousClient.disconnect()
        _status.value = MqttSubscriberStatus.DISCONNECTED
    }
}

package mx.utng.cala.rutalibre.data.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import mx.utng.cala.rutalibre.BuildConfig
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Estados posibles de la conexión MQTT del usuario. */
enum class MqttConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/** Estado observable de la conexión MQTT y su posible mensaje de error. */
data class MqttConnectionState(
    val status: MqttConnectionStatus = MqttConnectionStatus.DISCONNECTED,
    val error: String? = null
)

/** Mensaje MQTT recibido desde el tópico asociado con el usuario. */
data class MqttEvent(
    val topic: String,
    val payload: String
)

/** Administra la conexión segura a HiveMQ y distribuye eventos en tiempo real. */
class MqttManager {
    private val _connectionState = MutableStateFlow(MqttConnectionState())
    val connectionState: StateFlow<MqttConnectionState> = _connectionState

    private val _events = MutableSharedFlow<MqttEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<MqttEvent> = _events

    private var client: Mqtt3AsyncClient? = null
    private var currentUserId: Int? = null

    /** Conecta al broker, autentica la cuenta y suscribe sus eventos personales. */
    fun connect(userId: Int) {
        if (currentUserId == userId && client?.state == MqttClientState.CONNECTED) return

        disconnect()
        if (
            BuildConfig.MQTT_HOST.isBlank() ||
            BuildConfig.MQTT_USERNAME.isBlank() ||
            BuildConfig.MQTT_PASSWORD.isBlank()
        ) {
            _connectionState.value = MqttConnectionState(
                MqttConnectionStatus.ERROR,
                "Falta configurar MQTT en local.properties"
            )
            return
        }

        currentUserId = userId
        _connectionState.value = MqttConnectionState(MqttConnectionStatus.CONNECTING)

        val newClient = Mqtt3Client.builder()
            .identifier("ruta-libre-android-${UUID.randomUUID()}")
            .serverHost(BuildConfig.MQTT_HOST)
            .serverPort(BuildConfig.MQTT_PORT)
            .sslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()

        client = newClient
        newClient.connectWith()
            .simpleAuth()
                .username(BuildConfig.MQTT_USERNAME)
                .password(BuildConfig.MQTT_PASSWORD.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
            .cleanSession(true)
            .keepAlive(30)
            .send()
            .whenComplete { _, connectError ->
                if (connectError != null) {
                    _connectionState.value = MqttConnectionState(
                        MqttConnectionStatus.ERROR,
                        connectError.cause?.message ?: connectError.message
                    )
                    return@whenComplete
                }

                subscribeToUser(newClient, userId)
            }
    }

    /** Suscribe el cliente al árbol de tópicos del usuario y publica los mensajes recibidos. */
    private fun subscribeToUser(mqttClient: Mqtt3AsyncClient, userId: Int) {
        mqttClient.subscribeWith()
            .topicFilter("rutalibre/usuarios/$userId/#")
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                val topic = publish.topic.toString()
                val payload = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                Log.d("RutaLibreMQTT", "Mensaje recibido en $topic: $payload")
                _events.tryEmit(
                    MqttEvent(
                        topic = topic,
                        payload = payload
                    )
                )
            }
            .send()
            .whenComplete { _, subscribeError ->
                _connectionState.value = if (subscribeError == null) {
                    Log.d("RutaLibreMQTT", "Suscripción activa para el usuario $userId")
                    MqttConnectionState(MqttConnectionStatus.CONNECTED)
                } else {
                    MqttConnectionState(
                        MqttConnectionStatus.ERROR,
                        subscribeError.cause?.message ?: subscribeError.message
                    )
                }
            }
    }

    /** Cierra la conexión MQTT actual y restablece el estado desconectado. */
    fun disconnect() {
        val currentClient = client
        client = null
        currentUserId = null
        if (currentClient?.state == MqttClientState.CONNECTED) {
            currentClient.disconnect()
        }
        _connectionState.value = MqttConnectionState()
    }
}

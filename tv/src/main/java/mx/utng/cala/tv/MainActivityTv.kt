package mx.utng.cala.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import mx.utng.cala.tv.ui.navigation.TvNavGraph
import mx.utng.cala.tv.ui.theme.RutaLibreTheme
import mx.utng.cala.core.data.mqtt.MqttConfig
import mx.utng.cala.core.data.mqtt.MqttSubscriber
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.utng.cala.tv.data.TvIdentityStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class MainActivityTv : ComponentActivity() {
    private var linkedUserId: Int? = null
    private var remoteLogoutSignal by mutableIntStateOf(0)
    private lateinit var identityStore: TvIdentityStore
    private val mqttSubscriber by lazy {
        MqttSubscriber(
            config = MqttConfig(
                host = BuildConfig.MQTT_HOST,
                port = BuildConfig.MQTT_PORT,
                username = BuildConfig.MQTT_USERNAME,
                password = BuildConfig.MQTT_PASSWORD,
                clientPrefix = "ruta-libre-tv"
            ),
            logTag = "RutaLibreTvMQTT"
        )
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        identityStore = TvIdentityStore(this)
        lifecycleScope.launch {
            mqttSubscriber.events.collect { event ->
                val cerrarTodas = event.topic.endsWith("/sesion/cerrada")
                val cerrarEsta = identityStore.idDispositivo?.let {
                    event.topic.endsWith("/dispositivos/$it/desvinculado")
                } == true
                if (cerrarTodas || cerrarEsta) {
                    identityStore.clear()
                    linkedUserId = null
                    mqttSubscriber.disconnect()
                    remoteLogoutSignal += 1
                }
            }
        }
        setContent {
            RutaLibreTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    val navController = rememberNavController()
                    TvNavGraph(
                        navController = navController,
                        remoteLogoutSignal = remoteLogoutSignal,
                        onUsuarioVinculado = { idUsuario ->
                            linkedUserId = idUsuario
                            if (idUsuario != null) mqttSubscriber.connect(idUsuario)
                            else mqttSubscriber.disconnect()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        linkedUserId?.let(mqttSubscriber::connect)
    }

    override fun onStop() {
        mqttSubscriber.disconnect()
        super.onStop()
    }
}

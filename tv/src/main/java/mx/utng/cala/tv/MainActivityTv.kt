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

class MainActivityTv : ComponentActivity() {
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
        setContent {
            RutaLibreTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    val navController = rememberNavController()
                    TvNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mqttSubscriber.connect(BuildConfig.MQTT_USER_ID)
    }

    override fun onStop() {
        mqttSubscriber.disconnect()
        super.onStop()
    }
}

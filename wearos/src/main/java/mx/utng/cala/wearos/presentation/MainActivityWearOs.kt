package mx.utng.cala.wearos.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.AppScaffold
import mx.utng.cala.wearos.presentation.navigation.WearNavGraph
import mx.utng.cala.wearos.presentation.theme.RutaLibreTheme
import mx.utng.cala.core.data.mqtt.MqttConfig
import mx.utng.cala.core.data.mqtt.MqttSubscriber
import mx.utng.cala.wearos.BuildConfig

class MainActivityWearOs : ComponentActivity() {
    private val mqttSubscriber by lazy {
        MqttSubscriber(
            config = MqttConfig(
                host = BuildConfig.MQTT_HOST,
                port = BuildConfig.MQTT_PORT,
                username = BuildConfig.MQTT_USERNAME,
                password = BuildConfig.MQTT_PASSWORD,
                clientPrefix = "ruta-libre-wear"
            ),
            logTag = "RutaLibreWearMQTT"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        if (permissions.any { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 100)
        }

        setContent {
            RutaLibreTheme {
                AppScaffold {
                    val navController = rememberNavController()
                    WearNavGraph(navController = navController)
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

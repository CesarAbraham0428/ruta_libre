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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import mx.utng.cala.wearos.data.WearIdentityStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.DispositivoRepository

class MainActivityWearOs : ComponentActivity(), DataClient.OnDataChangedListener {
    private lateinit var identityStore: WearIdentityStore
    private var linkedUserId by mutableStateOf<Int?>(null)
    private val dispositivoRepository = DispositivoRepository()
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
        identityStore = WearIdentityStore(this)
        linkedUserId = identityStore.idUsuario
        lifecycleScope.launch {
            mqttSubscriber.events.collect { event ->
                val cerrarTodas = event.topic.endsWith("/sesion/cerrada")
                val cerrarEsta = identityStore.idDispositivo?.let {
                    event.topic.endsWith("/dispositivos/$it/desvinculado")
                } == true
                if (cerrarTodas || cerrarEsta) limpiarSesionLocal()
            }
        }
        
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
                    WearNavGraph(
                        navController = navController,
                        idUsuario = linkedUserId,
                        onCerrarSesion = ::cerrarSesionWear
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dataClient = Wearable.getDataClient(this)
        dataClient.addListener(this)
        dataClient.dataItems.addOnSuccessListener { items ->
            items.forEach { item -> readIdentity(item.uri.path, DataMapItem.fromDataItem(item)) }
            items.release()
        }
        linkedUserId?.let(mqttSubscriber::connect)
        identityStore.token?.let { token ->
            lifecycleScope.launch {
                if (dispositivoRepository.validarSesionDispositivo(token).isFailure) limpiarSesionLocal()
            }
        }
    }

    override fun onStop() {
        Wearable.getDataClient(this).removeListener(this)
        mqttSubscriber.disconnect()
        super.onStop()
    }

    override fun onDataChanged(events: DataEventBuffer) {
        events.filter { it.type == DataEvent.TYPE_CHANGED }.forEach { event ->
            readIdentity(event.dataItem.uri.path, DataMapItem.fromDataItem(event.dataItem))
        }
    }

    private fun readIdentity(path: String?, item: DataMapItem) {
        if (path != "/ruta-libre/identity") return
        val idUsuario = item.dataMap.getInt("idUsuario", -1)
        val idDispositivo = item.dataMap.getString("idDispositivo")
        val token = item.dataMap.getString("token")
        if (idUsuario > 0 && !idDispositivo.isNullOrBlank() && !token.isNullOrBlank()) {
            identityStore.save(idUsuario, idDispositivo, token)
            linkedUserId = idUsuario
            mqttSubscriber.connect(idUsuario)
        }
    }

    private fun cerrarSesionWear() {
        lifecycleScope.launch {
            identityStore.token?.let { dispositivoRepository.cerrarSesionDispositivo(it) }
            limpiarSesionLocal()
        }
    }

    private fun limpiarSesionLocal() {
        identityStore.clear()
        linkedUserId = null
        mqttSubscriber.disconnect()
    }
}

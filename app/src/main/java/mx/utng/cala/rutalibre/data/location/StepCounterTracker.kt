package mx.utng.cala.rutalibre.data.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Adapta el sensor de pasos del dispositivo a un flujo de pasos acumulados. */
class StepCounterTracker(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isAvailable: Boolean
        get() = stepCounter != null

    val isEmulator: Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator") ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true)

    /** Emite el contador global de pasos mientras el sensor esté disponible. */
    fun cumulativeSteps(): Flow<Long> = callbackFlow {
        val sensor = stepCounter
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            /** Envía el valor acumulado reportado por el sensor de pasos. */
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values.firstOrNull()?.toLong() ?: return)
            }

            /** El contador no necesita reaccionar a cambios en la precisión del sensor. */
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

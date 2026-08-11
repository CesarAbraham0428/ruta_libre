package mx.utng.cala.wearos.presentation.viewmodel

import android.content.Context
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

/** Encapsula Health Services para iniciar, observar y detener una carrera. */
class HealthServicesManager(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val exerciseClient = healthServicesClient.exerciseClient

    /** Indica si el reloj soporta el tipo de ejercicio de carrera. */
    suspend fun hasExerciseCapability(): Boolean {
        val capabilities = exerciseClient.getCapabilitiesAsync().await()
        return ExerciseType.RUNNING in capabilities.supportedExerciseTypes
    }

    /** Expone un flujo con las métricas recibidas durante el ejercicio activo. */
    fun exerciseStatus(): Flow<ExerciseUpdate> = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            /** Entrega al flujo la actualización más reciente de métricas. */
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                trySend(update)
            }

            /** No se requieren resúmenes de vuelta para este entrenamiento. */
            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
            /** Mantiene el callback sin acciones adicionales al registrarse. */
            override fun onRegistered() {}
            /** Mantiene el callback sin acciones adicionales si falla el registro. */
            override fun onRegistrationFailed(throwable: Throwable) {}
            /** No cambia la interfaz cuando cambia la disponibilidad de un dato. */
            override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {}
        }

        val config = ExerciseConfig.builder(ExerciseType.RUNNING)
            .setDataTypes(setOf(
                DataType.STEPS_TOTAL,
                DataType.CALORIES_TOTAL,
                DataType.DISTANCE_TOTAL
            ))
            .build()

        exerciseClient.startExerciseAsync(config).await()
        exerciseClient.setUpdateCallback(callback)

        awaitClose {
            // En un caso real, aquí detendríamos el ejercicio
            // exerciseClient.endExerciseAsync()
        }
    }
    
    /** Finaliza el ejercicio activo en Health Services. */
    suspend fun stopExercise() {
        exerciseClient.endExerciseAsync().await()
    }
}

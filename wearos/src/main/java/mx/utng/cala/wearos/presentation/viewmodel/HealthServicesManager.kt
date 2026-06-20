package mx.utng.cala.wearos.presentation.viewmodel

import android.content.Context
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class HealthServicesManager(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val exerciseClient = healthServicesClient.exerciseClient

    suspend fun hasExerciseCapability(): Boolean {
        val capabilities = exerciseClient.getCapabilitiesAsync().await()
        return ExerciseType.RUNNING in capabilities.supportedExerciseTypes
    }

    fun exerciseStatus(): Flow<ExerciseUpdate> = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                trySend(update)
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {}
            override fun onRegistered() {}
            override fun onRegistrationFailed(throwable: Throwable) {}
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
    
    suspend fun stopExercise() {
        exerciseClient.endExerciseAsync().await()
    }
}

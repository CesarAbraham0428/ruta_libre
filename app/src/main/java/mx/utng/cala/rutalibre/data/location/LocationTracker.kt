package mx.utng.cala.rutalibre.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationTracker(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_INTERVAL_MILLIS
    )
        .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MILLIS)
        .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
        .build()

    @SuppressLint("MissingPermission")
    fun locations(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    if (location.accuracy <= MAX_ACCEPTED_ACCURACY_METERS) {
                        trySend(location)
                    }
                }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }

        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val LOCATION_INTERVAL_MILLIS = 2_000L
        const val FASTEST_INTERVAL_MILLIS = 1_000L
        const val MIN_DISTANCE_METERS = 3f
        const val MAX_ACCEPTED_ACCURACY_METERS = 35f
    }
}

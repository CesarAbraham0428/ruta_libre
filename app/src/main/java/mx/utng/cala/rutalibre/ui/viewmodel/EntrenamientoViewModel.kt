package mx.utng.cala.rutalibre.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.core.data.model.Punto
import mx.utng.cala.core.data.repository.EntrenamientoRepository
import mx.utng.cala.rutalibre.data.location.LocationTracker
import mx.utng.cala.rutalibre.data.location.StepCounterTracker
import kotlin.math.roundToInt

data class EntrenamientoUiState(
    val isLoading: Boolean = false,
    val idEntrenamiento: Int? = null,
    val idRuta: Int? = null,
    val distancia: Double = 0.0,
    val pasos: Int = 0,
    val calorias: Int = 0,
    val tiempo: Int = 0,
    val estaActivo: Boolean = false,
    val ruta: List<Coordenada> = emptyList(),
    val ubicacionActual: Coordenada? = null,
    val metricasSimuladas: Boolean = false,
    val finalizado: Boolean = false,
    val error: String? = null
)

class EntrenamientoViewModel(application: Application) : AndroidViewModel(application) {
    private val entrenamientoRepository = EntrenamientoRepository()
    private val locationTracker = LocationTracker(application)
    private val stepCounterTracker = StepCounterTracker(application)

    private val _uiState = MutableStateFlow(EntrenamientoUiState())
    val uiState: StateFlow<EntrenamientoUiState> = _uiState

    private var locationJob: Job? = null
    private var initialLocationJob: Job? = null
    private var timerJob: Job? = null
    private var stepJob: Job? = null
    private var lastLocationElapsedRealtimeNanos: Long? = null
    private var baselineSteps: Long? = null
    private var pesoKg: Double = 70.0
    private var metricasExternasActivas = false

    fun prepararPantalla() {
        if (!_uiState.value.finalizado) return

        detenerCaptura()
        lastLocationElapsedRealtimeNanos = null
        _uiState.value = EntrenamientoUiState()
    }

    fun iniciar(idUsuario: Int, pesoKg: Double) {
        if (_uiState.value.estaActivo || _uiState.value.isLoading) return

        initialLocationJob?.cancel()
        viewModelScope.launch {
            lastLocationElapsedRealtimeNanos = null
            baselineSteps = null
            this@EntrenamientoViewModel.pesoKg = pesoKg
            metricasExternasActivas = false
            val ubicacionInicial = _uiState.value.ubicacionActual
            _uiState.value = EntrenamientoUiState(
                isLoading = true,
                ubicacionActual = ubicacionInicial
            )
            entrenamientoRepository.iniciar(idUsuario).fold(
                onSuccess = { entrenamiento ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            idEntrenamiento = entrenamiento.idEntrenamiento,
                            estaActivo = true,
                            ruta = ubicacionInicial?.let(::listOf) ?: emptyList()
                        )
                    }
                    iniciarCapturaDeUbicacion()
                    iniciarCapturaDePasos()
                    iniciarCronometro()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "No se pudo iniciar el entrenamiento")
                    }
                }
            )
        }
    }

    private fun iniciarCapturaDePasos() {
        stepJob?.cancel()

        if (stepCounterTracker.isEmulator || !stepCounterTracker.isAvailable) {
            _uiState.update { it.copy(metricasSimuladas = stepCounterTracker.isEmulator) }
            return
        }

        stepJob = viewModelScope.launch {
            try {
                stepCounterTracker.cumulativeSteps().collect { acumulado ->
                    if (baselineSteps == null) baselineSteps = acumulado
                    if (!metricasExternasActivas) {
                        val pasosEntrenamiento = (acumulado - (baselineSteps ?: acumulado))
                            .coerceAtLeast(0L)
                            .coerceAtMost(Int.MAX_VALUE.toLong())
                            .toInt()
                        _uiState.update { it.copy(pasos = pasosEntrenamiento) }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: SecurityException) {
                // Sin permiso, distancia y tiempo continúan funcionando.
            }
        }
    }

    fun cargarUbicacionInicial() {
        if (_uiState.value.ubicacionActual != null || _uiState.value.estaActivo) return

        initialLocationJob?.cancel()
        initialLocationJob = viewModelScope.launch {
            try {
                val location = locationTracker.locations().first()
                _uiState.update {
                    it.copy(
                        ubicacionActual = Coordenada(
                            longitud = location.longitude,
                            latitud = location.latitude
                        )
                    )
                }
            } catch (_: SecurityException) {
                // La pantalla solicitará el permiso y volverá a intentarlo.
            } catch (_: Exception) {
                // El mapa conserva su centro predeterminado si el GPS no está disponible.
            }
        }
    }

    private fun iniciarCapturaDeUbicacion() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            try {
                locationTracker.locations().collect(::registrarUbicacion)
            } catch (error: SecurityException) {
                _uiState.update { it.copy(error = "Se necesita permiso de ubicación para registrar la ruta") }
            } catch (error: CancellationException) {
                // Es el cierre normal del seguimiento al finalizar o salir de la pantalla.
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(error = error.message ?: "No se pudo obtener la ubicación") }
            }
        }
    }

    private fun iniciarCronometro() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.estaActivo) {
                delay(1_000)
                _uiState.update { state ->
                    if (state.estaActivo) state.copy(tiempo = state.tiempo + 1) else state
                }
            }
        }
    }

    private fun registrarUbicacion(location: Location) {
        if (!_uiState.value.estaActivo) return
        if (System.currentTimeMillis() - location.time > MAX_LOCATION_AGE_MILLIS) return
        if (
            lastLocationElapsedRealtimeNanos?.let {
                location.elapsedRealtimeNanos <= it
            } == true
        ) return

        val nueva = Coordenada(longitud = location.longitude, latitud = location.latitude)
        val state = _uiState.value
        val anterior = state.ruta.lastOrNull()
        val incrementoMetros = anterior?.let {
            val resultado = FloatArray(1)
            Location.distanceBetween(
                it.latitud,
                it.longitud,
                nueva.latitud,
                nueva.longitud,
                resultado
            )
            resultado[0].toDouble()
        } ?: 0.0

        val previousElapsedNanos = lastLocationElapsedRealtimeNanos
        val elapsedSeconds = previousElapsedNanos?.let {
            (location.elapsedRealtimeNanos - it).coerceAtLeast(1L) / 1_000_000_000.0
        }

        // Al cambiar la ubicación del emulador puede llegar primero un punto antiguo a miles de km.
        // Si apenas empezamos, sustituimos ese punto en lugar de dibujar una recta continental.
        if (anterior != null && state.ruta.size <= 2 && incrementoMetros > INITIAL_TELEPORT_METERS) {
            lastLocationElapsedRealtimeNanos = location.elapsedRealtimeNanos
            _uiState.update {
                it.copy(
                    ruta = listOf(nueva),
                    ubicacionActual = nueva,
                    distancia = 0.0,
                    pasos = 0,
                    calorias = 0
                )
            }
            return
        }

        val speedMetersPerSecond = elapsedSeconds?.takeIf { it > 0 }?.let { incrementoMetros / it }
        if (
            anterior != null &&
            incrementoMetros > MIN_JUMP_DISTANCE_METERS &&
            speedMetersPerSecond != null &&
            speedMetersPerSecond > MAX_RUNNING_SPEED_METERS_PER_SECOND
        ) {
            return
        }

        lastLocationElapsedRealtimeNanos = location.elapsedRealtimeNanos
        _uiState.update {
            val nuevaDistancia = it.distancia + (incrementoMetros / 1_000.0)
            val pasosCalculados = if (
                stepCounterTracker.isEmulator && !metricasExternasActivas
            ) {
                (nuevaDistancia * 1_000.0 / STRIDE_LENGTH_METERS).roundToInt()
            } else it.pasos
            val caloriasCalculadas = if (!metricasExternasActivas) {
                estimarCalorias(nuevaDistancia)
            } else it.calorias

            it.copy(
                ruta = it.ruta + nueva,
                ubicacionActual = nueva,
                distancia = nuevaDistancia,
                pasos = pasosCalculados,
                calorias = caloriasCalculadas,
                metricasSimuladas = stepCounterTracker.isEmulator && !metricasExternasActivas
            )
        }
    }

    private fun estimarCalorias(distanciaKm: Double): Int =
        (pesoKg * distanciaKm * CALORIES_PER_KG_KM).roundToInt()

    /** Punto de integración para las métricas que llegarán posteriormente mediante MQTT. */
    fun actualizarMetricas(pasos: Int, calorias: Int) {
        metricasExternasActivas = true
        stepJob?.cancel()
        _uiState.update {
            it.copy(pasos = pasos, calorias = calorias, metricasSimuladas = false)
        }
    }

    fun finalizar() {
        val state = _uiState.value
        val idEntrenamiento = state.idEntrenamiento ?: return
        if (state.isLoading || state.finalizado) return

        if (state.estaActivo) detenerCaptura()
        _uiState.update { it.copy(estaActivo = false, isLoading = true) }

        val inicio = state.ruta.firstOrNull()?.let { Punto(it.longitud, it.latitud) } ?: Punto()
        val fin = state.ruta.lastOrNull()?.let { Punto(it.longitud, it.latitud) } ?: Punto()

        viewModelScope.launch {
            entrenamientoRepository.finalizar(
                idEntrenamiento = idEntrenamiento,
                pasos = state.pasos,
                calorias = state.calorias,
                distancia = state.distancia,
                tiempo = state.tiempo,
                coordenadas = state.ruta,
                puntoInicio = inicio,
                puntoFin = fin
            ).fold(
                onSuccess = { entrenamiento ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            idRuta = entrenamiento.idRuta,
                            finalizado = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "No se pudo guardar el entrenamiento"
                        )
                    }
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun detenerCaptura() {
        initialLocationJob?.cancel()
        initialLocationJob = null
        locationJob?.cancel()
        locationJob = null
        timerJob?.cancel()
        timerJob = null
        stepJob?.cancel()
        stepJob = null
    }

    override fun onCleared() {
        detenerCaptura()
        super.onCleared()
    }

    private companion object {
        const val MAX_LOCATION_AGE_MILLIS = 15_000L
        // El emulador puede saltar desde su última posición al punto A al reproducir
        // una ruta. Durante los primeros puntos, no contamos ese traslado artificial.
        const val INITIAL_TELEPORT_METERS = 100.0
        const val MIN_JUMP_DISTANCE_METERS = 50.0
        const val MAX_RUNNING_SPEED_METERS_PER_SECOND = 12.0
        const val STRIDE_LENGTH_METERS = 0.75
        // Factor aproximado de kcal por kg y km para sesiones mixtas de running y caminata.
        // Se pondera hacia running, por lo que se utiliza 0.90 como estimación intermedia.
        const val CALORIES_PER_KG_KM = 0.90
    }
}

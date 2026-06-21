package mx.utng.cala.wearos.presentation.viewmodel

import android.app.Application
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataPoint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.model.Punto
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.core.data.repository.EntrenamientoRepository
import mx.utng.cala.core.data.repository.MetaRepository

data class MetaCompletada(
    val tipoMeta: TipoMeta,
    val valorObjetivo: Double
)

data class WearEntrenamientoUiState(
    val estaActivo: Boolean = false,
    val idEntrenamiento: Int? = null,
    val distancia: Double = 0.0,
    val pasos: Int = 0,
    val calorias: Int = 0,
    val tiempo: Int = 0,
    val metasCompletadas: List<MetaCompletada> = emptyList(),
    val mostrarMetaCompletada: Boolean = false,
    val metaActual: MetaCompletada? = null
)

class WearEntrenamientoViewModel(application: Application) : AndroidViewModel(application) {

    private val entrenamientoRepository = EntrenamientoRepository()
    private val metaRepository = MetaRepository()
    private val healthServicesManager = HealthServicesManager(application)
    private val _uiState = MutableStateFlow(WearEntrenamientoUiState())
    val uiState: StateFlow<WearEntrenamientoUiState> = _uiState

    private var fechaInicioMillis: Long = 0L

    // Metas hardcodeadas para pruebas
    private val metasPrueba = listOf(
        MetaCompletada(TipoMeta.DISTANCIA, 0.20),
        MetaCompletada(TipoMeta.PASOS, 30.0),
        MetaCompletada(TipoMeta.CALORIAS, 12.0)
    )
    private val metasAlcanzadas = mutableSetOf<TipoMeta>()

    fun iniciar(idUsuario: Int) {
        fechaInicioMillis = System.currentTimeMillis()
        metasAlcanzadas.clear()
        _uiState.value = WearEntrenamientoUiState(
            estaActivo = true,
            idEntrenamiento = -1
        )

        viewModelScope.launch {
            launch {
                healthServicesManager.exerciseStatus().collect { update ->
                    val data = update.latestMetrics
                    
                    val pasos = data.getData(DataType.STEPS_TOTAL)?.total?.toInt() ?: _uiState.value.pasos
                    
                    // Conversión de metros (reloj) a kilómetros (interfaz)
                    val metros = data.getData(DataType.DISTANCE_TOTAL)?.total ?: (_uiState.value.distancia * 1000)
                    val kilometros = metros / 1000.0
                    
                    val calorias = data.getData(DataType.CALORIES_TOTAL)?.total?.toInt() ?: _uiState.value.calorias
                    
                    actualizarMetricas(pasos, calorias, kilometros)
                }
            }

            entrenamientoRepository.iniciar(idUsuario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(idEntrenamiento = it.idEntrenamiento)
                },
                onFailure = { }
            )
        }
    }

    fun actualizarMetricas(pasos: Int, calorias: Int, distancia: Double) {
        if (!_uiState.value.estaActivo) return

        val tiempoSegundos = if (fechaInicioMillis > 0) {
            ((System.currentTimeMillis() - fechaInicioMillis) / 1000).toInt()
        } else 0
        _uiState.value = _uiState.value.copy(
            pasos = pasos,
            calorias = calorias,
            distancia = distancia,
            tiempo = tiempoSegundos
        )

        // Verificar metas hardcodeadas en tiempo real
        verificarMetasPrueba(distancia, pasos, calorias)
    }

    private fun verificarMetasPrueba(distancia: Double, pasos: Int, calorias: Int) {
        if (_uiState.value.mostrarMetaCompletada) return

        val metasCompletadas = mutableListOf<MetaCompletada>()

        for (meta in metasPrueba) {
            if (meta.tipoMeta in metasAlcanzadas) continue

            val alcanzada = when (meta.tipoMeta) {
                TipoMeta.DISTANCIA -> distancia >= meta.valorObjetivo
                TipoMeta.PASOS -> pasos >= meta.valorObjetivo.toInt()
                TipoMeta.CALORIAS -> calorias >= meta.valorObjetivo.toInt()
                TipoMeta.TIEMPO -> false
            }

            if (alcanzada) {
                metasAlcanzadas.add(meta.tipoMeta)
                metasCompletadas.add(meta)
            }
        }

        if (metasCompletadas.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                metasCompletadas = metasCompletadas,
                mostrarMetaCompletada = true,
                metaActual = metasCompletadas.first()
            )
        }
    }

    fun finalizar(idUsuario: Int, onResult: () -> Unit = {}) {
        val state = _uiState.value
        _uiState.value = _uiState.value.copy(estaActivo = false)
        
        val idEntrenamiento = state.idEntrenamiento
        
        viewModelScope.launch {
            healthServicesManager.stopExercise()
            
            if (idEntrenamiento == null || idEntrenamiento == -1) {
                onResult()
                return@launch
            }

            val tiempo = if (fechaInicioMillis > 0) {
                ((System.currentTimeMillis() - fechaInicioMillis) / 1000).toInt()
            } else state.tiempo

            entrenamientoRepository.finalizar(
                idEntrenamiento = idEntrenamiento,
                pasos = state.pasos,
                calorias = state.calorias,
                distancia = state.distancia,
                tiempo = tiempo,
                coordenadas = emptyList(),
                puntoInicio = Punto(0.0, 0.0),
                puntoFin = Punto(0.0, 0.0)
            ).fold(
                onSuccess = {
                    checkMetasCompletadas(idUsuario)
                    onResult()
                },
                onFailure = { onResult() }
            )
        }
    }

    private fun checkMetasCompletadas(idUsuario: Int) {
        viewModelScope.launch {
            metaRepository.getMetas(idUsuario).fold(
                onSuccess = { metas ->
                    val completadas = metas.filter { meta ->
                        !meta.terminada && meta.valorActual >= meta.valorObjetivo
                    }.map { meta ->
                        MetaCompletada(
                            tipoMeta = TipoMeta.valueOf(meta.tipoMeta.uppercase()),
                            valorObjetivo = meta.valorObjetivo
                        )
                    }
                    if (completadas.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            metasCompletadas = completadas,
                            mostrarMetaCompletada = true,
                            metaActual = completadas.first()
                        )
                    }
                },
                onFailure = { }
            )
        }
    }

    fun aceptarMetaCompletada() {
        val restantes = _uiState.value.metasCompletadas.drop(1)
        if (restantes.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                metasCompletadas = restantes,
                metaActual = restantes.first()
            )
        } else {
            _uiState.value = _uiState.value.copy(
                metasCompletadas = emptyList(),
                mostrarMetaCompletada = false,
                metaActual = null
            )
        }
    }
}

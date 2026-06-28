package mx.utng.cala.wearos.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.health.services.client.data.DataType
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.MetaResponse
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

    // Metas reales cargadas desde el servidor para monitoreo en tiempo real
    private val metasUsuario = mutableListOf<MetaResponse>()
    private val metasAlcanzadas = mutableSetOf<TipoMeta>()

    fun iniciar(idUsuario: Int) {
        fechaInicioMillis = System.currentTimeMillis()
        metasAlcanzadas.clear()
        synchronized(metasUsuario) {
            metasUsuario.clear()
        }
        _uiState.value = WearEntrenamientoUiState(
            estaActivo = true,
            idEntrenamiento = null
        )

        viewModelScope.launch {
            metaRepository.getMetas(idUsuario).fold(
                onSuccess = { metas ->
                    synchronized(metasUsuario) {
                        metasUsuario.clear()
                        metasUsuario.addAll(metas.filter { !it.terminada })
                    }
                },
                onFailure = { e -> Log.e("WearVM", "Error al cargar metas", e) }
            )

            launch {
                try {
                    healthServicesManager.exerciseStatus().collect { update ->
                        val data = update.latestMetrics
                        
                        val pasos = data.getData(DataType.STEPS_TOTAL)?.total?.toInt() ?: _uiState.value.pasos
                        
                        val metros = data.getData(DataType.DISTANCE_TOTAL)?.total ?: (_uiState.value.distancia * 1000)
                        val kilometros = metros / 1000.0
                        
                        val calorias = data.getData(DataType.CALORIES_TOTAL)?.total?.toInt() ?: _uiState.value.calorias
                        
                        actualizarMetricas(pasos, calorias, kilometros)
                    }
                } catch (e: Exception) {
                    Log.e("WearVM", "Error en health services", e)
                }
            }

            entrenamientoRepository.iniciar(idUsuario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(idEntrenamiento = it.idEntrenamiento)
                },
                onFailure = { e -> Log.e("WearVM", "Error al iniciar entrenamiento", e) }
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

        // Verificar metas del usuario en tiempo real
        verificarMetasUsuario(distancia, pasos, calorias, tiempoSegundos)
    }

    private fun verificarMetasUsuario(distancia: Double, pasos: Int, calorias: Int, tiempoSegundos: Int) {
        if (_uiState.value.mostrarMetaCompletada) return

        val completedGoalsList = mutableListOf<MetaCompletada>()

        synchronized(metasUsuario) {
            for (meta in metasUsuario) {
                val tipo = try {
                    TipoMeta.valueOf(meta.tipoMeta.uppercase())
                } catch (e: Exception) {
                    continue
                }

                if (tipo in metasAlcanzadas) continue

                val alcanzada = when (tipo) {
                    TipoMeta.DISTANCIA -> (meta.valorActual + distancia) >= meta.valorObjetivo
                    TipoMeta.PASOS -> (meta.valorActual + pasos) >= meta.valorObjetivo
                    TipoMeta.CALORIAS -> (meta.valorActual + calorias) >= meta.valorObjetivo
                    TipoMeta.TIEMPO -> (meta.valorActual + (tiempoSegundos / 60.0)) >= meta.valorObjetivo
                }

                if (alcanzada) {
                    metasAlcanzadas.add(tipo)
                    completedGoalsList.add(MetaCompletada(tipo, meta.valorObjetivo))
                }
            }
        }

        if (completedGoalsList.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                metasCompletadas = _uiState.value.metasCompletadas + completedGoalsList,
                mostrarMetaCompletada = true,
                metaActual = completedGoalsList.first()
            )
        }
    }

    fun finalizar(idUsuario: Int, onResult: () -> Unit = {}) {
        _uiState.value = _uiState.value.copy(estaActivo = false)

        viewModelScope.launch {
            try {
                healthServicesManager.stopExercise()
            } catch (e: Exception) {
                Log.e("WearVM", "Error al detener ejercicio", e)
            }

            val state = _uiState.value
            val idEntrenamiento = state.idEntrenamiento

            if (idEntrenamiento == null) {
                Log.e("WearVM", "No hay idEntrenamiento válido — no se guardarán los datos")
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
                    Log.d("WearVM", "Entrenamiento $idEntrenamiento finalizado correctamente")
                    checkMetasCompletadas(idUsuario)
                    onResult()
                },
                onFailure = { e ->
                    Log.e("WearVM", "Error al finalizar entrenamiento $idEntrenamiento", e)
                    onResult()
                }
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
                onFailure = { e -> Log.e("WearVM", "Error al verificar metas", e) }
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

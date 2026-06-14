package mx.utng.cala.wearos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.core.data.model.Coordenada
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

class WearEntrenamientoViewModel : ViewModel() {

    private val entrenamientoRepository = EntrenamientoRepository()
    private val metaRepository = MetaRepository()
    private val _uiState = MutableStateFlow(WearEntrenamientoUiState())
    val uiState: StateFlow<WearEntrenamientoUiState> = _uiState

    private var fechaInicioMillis: Long = 0L

    fun iniciar(idUsuario: Int) {
        // ACTIVACIÓN INSTANTÁNEA LOCAL
        fechaInicioMillis = System.currentTimeMillis()
        _uiState.value = WearEntrenamientoUiState(
            estaActivo = true,
            idEntrenamiento = -1 // ID temporal para que 'finalizar' funcione siempre
        )

        viewModelScope.launch {
            entrenamientoRepository.iniciar(idUsuario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        idEntrenamiento = it.idEntrenamiento
                    )
                },
                onFailure = { 
                    // Si falla el servidor, nos quedamos con el ID temporal
                    // para permitir que el usuario pueda finalizar la sesión.
                }
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
    }

    fun finalizar(idUsuario: Int, onResult: () -> Unit = {}) {
        val state = _uiState.value
        
        // DETENCIÓN INSTANTÁNEA LOCAL
        _uiState.value = _uiState.value.copy(estaActivo = false)
        
        val idEntrenamiento = state.idEntrenamiento
        if (idEntrenamiento == null) {
            onResult()
            return
        }

        val tiempo = if (fechaInicioMillis > 0) {
            ((System.currentTimeMillis() - fechaInicioMillis) / 1000).toInt()
        } else state.tiempo

        viewModelScope.launch {
            // Solo llamamos al servidor si el ID no es el temporal (-1)
            if (idEntrenamiento != -1) {
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
            } else {
                onResult()
            }
        }
    }

    private fun checkMetasCompletadas(idUsuario: Int) {
        viewModelScope.launch {
            metaRepository.getMetas(idUsuario).fold(
                onSuccess = { metas ->
                    val state = _uiState.value
                    val completadas = metas.filter { meta ->
                        !meta.terminada && meta.valorActual >= meta.valorObjetivo
                    }.map { meta ->
                        MetaCompletada(
                            tipoMeta = TipoMeta.valueOf(meta.tipoMeta),
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
                onFailure = { /* manejar error */ }
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

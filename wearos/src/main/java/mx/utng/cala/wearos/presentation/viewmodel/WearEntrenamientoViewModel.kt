package mx.utng.cala.wearos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.EntrenamientoRepository

data class WearEntrenamientoUiState(
    val estaActivo: Boolean = false,
    val idEntrenamiento: Int? = null,
    val distancia: Double = 0.0,
    val pasos: Int = 0,
    val calorias: Int = 0,
    val tiempo: Int = 0
)

class WearEntrenamientoViewModel : ViewModel() {

    private val repository = EntrenamientoRepository()
    private val _uiState = MutableStateFlow(WearEntrenamientoUiState())
    val uiState: StateFlow<WearEntrenamientoUiState> = _uiState

    fun iniciar(idUsuario: Int) {
        viewModelScope.launch {
            repository.iniciar(idUsuario).fold(
                onSuccess = {
                    _uiState.value = WearEntrenamientoUiState(
                        estaActivo = true, idEntrenamiento = it.idEntrenamiento
                    )
                },
                onFailure = { /* manejar error */ }
            )
        }
    }

    fun actualizarMetricas(pasos: Int, calorias: Int, distancia: Double, tiempo: Int) {
        _uiState.value = _uiState.value.copy(
            pasos = pasos, calorias = calorias, distancia = distancia, tiempo = tiempo
        )
    }
}

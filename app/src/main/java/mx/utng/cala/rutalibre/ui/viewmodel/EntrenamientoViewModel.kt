package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.model.Coordenada
import mx.utng.cala.core.data.model.Punto
import mx.utng.cala.core.data.repository.EntrenamientoRepository
import mx.utng.cala.core.data.repository.RutaRepository

data class EntrenamientoUiState(
    val isLoading: Boolean = false,
    val idEntrenamiento: Int? = null,
    val distancia: Double = 0.0,
    val pasos: Int = 0,
    val calorias: Int = 0,
    val tiempo: Int = 0,
    val estaActivo: Boolean = false,
    val error: String? = null
)

class EntrenamientoViewModel : ViewModel() {

    private val entrenamientoRepo = EntrenamientoRepository()
    private val rutaRepo = RutaRepository()
    private val _uiState = MutableStateFlow(EntrenamientoUiState())
    val uiState: StateFlow<EntrenamientoUiState> = _uiState

    fun iniciar(idUsuario: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            entrenamientoRepo.iniciar(idUsuario).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, idEntrenamiento = it.idEntrenamiento, estaActivo = true
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun finalizar(idEntrenamiento: Int, pasos: Int, calorias: Int, distancia: Double, tiempo: Int, coordenadas: List<Coordenada>, inicio: Punto, fin: Punto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            entrenamientoRepo.finalizar(idEntrenamiento, pasos, calorias, distancia, tiempo, coordenadas, inicio, fin).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, estaActivo = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }
}

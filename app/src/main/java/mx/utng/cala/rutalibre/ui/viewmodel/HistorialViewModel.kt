package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.EntrenamientoResponse
import mx.utng.cala.core.data.repository.EntrenamientoRepository

data class HistorialUiState(
    val isLoading: Boolean = false,
    val entrenamientos: List<EntrenamientoResponse> = emptyList(),
    val error: String? = null
)

class HistorialViewModel : ViewModel() {
    private val repository = EntrenamientoRepository()
    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState

    fun cargar(idUsuario: Int, forzar: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (!forzar && _uiState.value.entrenamientos.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getHistorial(idUsuario).fold(
                onSuccess = { entrenamientos ->
                    _uiState.value = HistorialUiState(entrenamientos = entrenamientos)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "No se pudo cargar el historial"
                        )
                    }
                }
            )
        }
    }
}

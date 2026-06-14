package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.ComparacionRendimientoResponse
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.repository.EntrenamientoRepository

data class DashboardUiState(
    val isLoading: Boolean = false,
    val semanal: DashboardSemanalResponse? = null,
    val comparacion: ComparacionRendimientoResponse? = null,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val repository = EntrenamientoRepository()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun cargarDashboard(idUsuario: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getDashboardSemanal(idUsuario).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, semanal = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun cargarComparacion(idUsuario: Int) {
        viewModelScope.launch {
            repository.getComparacion(idUsuario).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(comparacion = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }
}

package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.core.data.repository.MetaRepository

data class MetasUiState(
    val isLoading: Boolean = false,
    val metas: List<MetaResponse> = emptyList(),
    val isMetaCreated: Boolean = false,
    val error: String? = null
)

class MetasViewModel : ViewModel() {

    private val repository = MetaRepository()
    private val _uiState = MutableStateFlow(MetasUiState())
    val uiState: StateFlow<MetasUiState> = _uiState

    fun cargarMetas(idUsuario: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getMetas(idUsuario).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, metas = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun crearMeta(idUsuario: Int, tipo: TipoMeta, valor: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isMetaCreated = false)
            repository.crearMeta(idUsuario, tipo, valor).fold(
                onSuccess = { 
                    cargarMetas(idUsuario)
                    _uiState.value = _uiState.value.copy(isMetaCreated = true)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetMetaCreatedState() {
        _uiState.value = _uiState.value.copy(isMetaCreated = false)
    }
}

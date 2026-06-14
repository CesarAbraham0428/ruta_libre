package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.GrupoResponse
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.repository.GrupoRepository

data class GrupoUiState(
    val isLoading: Boolean = false,
    val grupos: List<GrupoResponse> = emptyList(),
    val miembros: List<MiembroGrupoResponse> = emptyList(),
    val error: String? = null
)

class GrupoViewModel : ViewModel() {

    private val repository = GrupoRepository()
    private val _uiState = MutableStateFlow(GrupoUiState())
    val uiState: StateFlow<GrupoUiState> = _uiState

    fun cargarGrupos(idUsuario: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getGrupos(idUsuario).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, grupos = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun crearGrupo(nombre: String, descripcion: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.crearGrupo(nombre, descripcion).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun unirseGrupo(idUsuario: Int, codigo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.unirseGrupo(idUsuario, codigo).fold(
                onSuccess = { cargarGrupos(idUsuario) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun cargarMiembros(idGrupo: Int) {
        viewModelScope.launch {
            repository.getMiembros(idGrupo).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(miembros = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }
}

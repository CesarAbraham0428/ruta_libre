package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.MiembroGrupoResponse
import mx.utng.cala.core.data.dto.response.RankingResponse
import mx.utng.cala.core.data.repository.GrupoRepository

data class GrupoTvUiState(
    val isLoading: Boolean = false,
    val miembros: List<MiembroGrupoResponse> = emptyList(),
    val ranking: RankingResponse? = null,
    val error: String? = null
)

class GrupoTvViewModel : ViewModel() {

    private val repository = GrupoRepository()
    private val _uiState = MutableStateFlow(GrupoTvUiState())
    val uiState: StateFlow<GrupoTvUiState> = _uiState

    fun cargarMiembros(idGrupo: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMiembros(idGrupo).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, miembros = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun cargarRanking(idGrupo: Int) {
        viewModelScope.launch {
            repository.getRanking(idGrupo).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(ranking = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }
}

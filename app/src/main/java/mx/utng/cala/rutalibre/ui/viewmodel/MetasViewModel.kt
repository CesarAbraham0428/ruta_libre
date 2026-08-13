package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.MetaResponse
import mx.utng.cala.core.data.model.TipoMeta
import mx.utng.cala.core.data.repository.MetaRepository

/** Estado de las metas del usuario y de las operaciones CRUD en curso. */
data class MetasUiState(
    val isLoading: Boolean = false,
    val metas: List<MetaResponse> = emptyList(),
    val isMetaCreated: Boolean = false,
    val isMetaUpdated: Boolean = false,
    val isMetaDeleted: Boolean = false,
    val error: String? = null
)

/** Administra la consulta, creación, edición y eliminación de metas personales. */
class MetasViewModel : ViewModel() {

    private val repository = MetaRepository()
    private val _uiState = MutableStateFlow(MetasUiState())
    val uiState: StateFlow<MetasUiState> = _uiState

    /** Carga las metas registradas para el usuario. */
    fun cargarMetas(idUsuario: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getMetas(idUsuario).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, metas = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    /** Crea una meta y actualiza el listado al terminar correctamente. */
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

    /** Cambia el valor objetivo de una meta existente. */
    fun editarMeta(idUsuario: Int, idMetas: Int, nuevoValor: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isMetaUpdated = false)
            repository.actualizarMeta(idMetas, nuevoValor).fold(
                onSuccess = {
                    cargarMetas(idUsuario)
                    _uiState.value = _uiState.value.copy(isMetaUpdated = true)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    /** Elimina una meta y refresca las metas restantes del usuario. */
    fun eliminarMeta(idUsuario: Int, idMetas: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isMetaDeleted = false)
            repository.eliminarMeta(idMetas).fold(
                onSuccess = {
                    cargarMetas(idUsuario)
                    _uiState.value = _uiState.value.copy(isMetaDeleted = true)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    /** Limpia el error de la última operación de metas. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Consume la bandera que indica que una meta fue creada. */
    fun resetMetaCreatedState() {
        _uiState.value = _uiState.value.copy(isMetaCreated = false)
    }

    /** Consume la bandera que indica que una meta fue actualizada. */
    fun resetMetaUpdatedState() {
        _uiState.value = _uiState.value.copy(isMetaUpdated = false)
    }

    /** Consume la bandera que indica que una meta fue eliminada. */
    fun resetMetaDeletedState() {
        _uiState.value = _uiState.value.copy(isMetaDeleted = false)
    }
}

package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.DispositivoResponse
import mx.utng.cala.core.data.repository.DispositivoRepository

/** Estado de carga, listado y operaciones sobre dispositivos vinculados. */
data class DispositivosUiState(
    val cargando: Boolean = false,
    val dispositivos: List<DispositivoResponse> = emptyList(),
    val error: String? = null,
    val sesionesCerradas: Boolean = false
)

/** Gestiona la consulta y revocación de dispositivos asociados a la cuenta. */
class DispositivosViewModel : ViewModel() {
    private val repository = DispositivoRepository()
    private val _uiState = MutableStateFlow(DispositivosUiState())
    val uiState: StateFlow<DispositivosUiState> = _uiState

    /** Obtiene del backend los dispositivos vinculados al usuario autenticado. */
    fun cargar(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            repository.listar(token).fold(
                onSuccess = { _uiState.value = DispositivosUiState(dispositivos = it) },
                onFailure = { _uiState.value = _uiState.value.copy(cargando = false, error = it.message) }
            )
        }
    }

    /** Desvincula un dispositivo y actualiza el listado al terminar. */
    fun desvincular(token: String, idDispositivo: String) {
        viewModelScope.launch {
            repository.desvincular(token, idDispositivo).fold(
                onSuccess = { cargar(token) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    /** Solicita el cierre de todas las sesiones activas de dispositivos. */
    fun cerrarTodas(token: String) {
        viewModelScope.launch {
            repository.cerrarTodasLasSesiones(token).fold(
                onSuccess = { _uiState.value = DispositivosUiState(sesionesCerradas = true) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    /** Consume la notificación de cierre masivo para evitar mostrarla repetidamente. */
    fun consumirCierreDeSesiones() {
        _uiState.value = _uiState.value.copy(sesionesCerradas = false)
    }
}

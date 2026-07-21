package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.DispositivoRepository

data class VincularDispositivoUiState(
    val cargando: Boolean = false,
    val vinculado: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class VincularDispositivoViewModel : ViewModel() {
    private val repository = DispositivoRepository()
    private val _uiState = MutableStateFlow(VincularDispositivoUiState())
    val uiState: StateFlow<VincularDispositivoUiState> = _uiState

    fun reiniciar() {
        _uiState.value = VincularDispositivoUiState()
    }

    fun vincular(token: String, codigo: String) {
        val normalized = codigo.trim().uppercase()
        if (normalized.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Escribe el código de 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = VincularDispositivoUiState(cargando = true)
            repository.vincularCodigo(token, normalized).fold(
                onSuccess = {
                    _uiState.value = VincularDispositivoUiState(
                        vinculado = true,
                        mensaje = "${it.nombre ?: "Dispositivo"} quedó vinculado a tu cuenta"
                    )
                },
                onFailure = {
                    _uiState.value = VincularDispositivoUiState(error = it.message)
                }
            )
        }
    }
}

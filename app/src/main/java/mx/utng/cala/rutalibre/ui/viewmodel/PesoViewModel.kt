package mx.utng.cala.rutalibre.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.repository.RepositorioUsuario

/** Estado de guardado del peso inicial del usuario. */
data class PesoUiState(
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null
)

/** Valida y guarda el peso requerido para calcular métricas de entrenamiento. */
class PesoViewModel : ViewModel() {
    private val repositorio = RepositorioUsuario()
    private val _estado = MutableStateFlow(PesoUiState())
    val estado: StateFlow<PesoUiState> = _estado

    /** Persiste un peso dentro del rango permitido y comunica el resultado a la UI. */
    fun guardarPeso(idUsuario: Int, pesoKg: Double) {
        if (_estado.value.guardando || pesoKg !in 20.0..300.0) return

        viewModelScope.launch {
            _estado.value = PesoUiState(guardando = true)
            repositorio.actualizarPeso(idUsuario, pesoKg).fold(
                onSuccess = { _estado.value = PesoUiState(guardado = true) },
                onFailure = { error ->
                    _estado.value = PesoUiState(error = error.message ?: "No se pudo guardar el peso")
                }
            )
        }
    }
}

package mx.utng.cala.tv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.utng.cala.core.data.dto.response.ComparacionRendimientoResponse
import mx.utng.cala.core.data.dto.response.DashboardSemanalResponse
import mx.utng.cala.core.data.dto.response.RespuestaDashboardMensual
import mx.utng.cala.core.data.repository.EntrenamientoRepository

enum class PeriodoDashboard {
    SEMANAL,
    MENSUAL
}

data class EstadoUiDashboard(
    val estaCargando: Boolean = false,
    val semanal: DashboardSemanalResponse? = null,
    val comparacionSemanal: ComparacionRendimientoResponse? = null,
    val mensual: RespuestaDashboardMensual? = null,
    val comparacionMensual: ComparacionRendimientoResponse? = null,
    val periodoSeleccionado: PeriodoDashboard = PeriodoDashboard.SEMANAL,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {

    private val repositorio = EntrenamientoRepository()
    private val _estadoUi = MutableStateFlow(EstadoUiDashboard())
    val estadoUi: StateFlow<EstadoUiDashboard> = _estadoUi

    fun cargarDashboardSemanal(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true)
            repositorio.getDashboardSemanal(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, semanal = it) },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, error = it.message) }
            )
        }
    }

    fun cargarComparacionSemanal(idUsuario: Int) {
        viewModelScope.launch {
            repositorio.getComparacion(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(comparacionSemanal = it) },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(error = it.message) }
            )
        }
    }

    fun cargarDashboardMensual(idUsuario: Int) {
        viewModelScope.launch {
            _estadoUi.value = _estadoUi.value.copy(estaCargando = true)
            repositorio.obtenerDashboardMensual(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, mensual = it) },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(estaCargando = false, error = it.message) }
            )
        }
    }

    fun cargarComparacionMensual(idUsuario: Int) {
        viewModelScope.launch {
            repositorio.obtenerComparacionMensual(idUsuario).fold(
                onSuccess = { _estadoUi.value = _estadoUi.value.copy(comparacionMensual = it) },
                onFailure = { _estadoUi.value = _estadoUi.value.copy(error = it.message) }
            )
        }
    }

    fun cambiarPeriodo(nuevoPeriodo: PeriodoDashboard, idUsuario: Int) {
        _estadoUi.value = _estadoUi.value.copy(periodoSeleccionado = nuevoPeriodo)
        if (nuevoPeriodo == PeriodoDashboard.SEMANAL) {
            cargarDashboardSemanal(idUsuario)
            cargarComparacionSemanal(idUsuario)
        } else {
            cargarDashboardMensual(idUsuario)
            cargarComparacionMensual(idUsuario)
        }
    }
}
